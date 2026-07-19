const API = '/api';

const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => document.querySelectorAll(sel);

let categories = [];
let userSettings = { userName: 'User', monthlyIncome: 0 };

const CATEGORY_ICONS = {
  Food: '🍔', Transport: '🚗', Rent: '🏠', Entertainment: '🎬',
  Shopping: '🛍', Utilities: '💡', Health: '💊', Other: '📦',
};

function currentMonth() {
  return $('#report-month').value;
}

function setDefaultMonth() {
  const now = new Date();
  const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  $('#report-month').value = month;
  $('#expense-date').value = now.toISOString().slice(0, 10);
  setTimeOfDay();
}

function setTimeOfDay() {
  const hour = new Date().getHours();
  const period = hour < 12 ? 'morning' : hour < 17 ? 'afternoon' : 'evening';
  $('#time-of-day').textContent = period;
}

function formatMoney(amount) {
  return '₹' + Number(amount).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function showToast(message, type = 'success') {
  const toast = $('#toast');
  toast.textContent = message;
  toast.className = `toast ${type}`;
  setTimeout(() => toast.classList.add('hidden'), 3000);
}

async function api(path, options = {}) {
  const res = await fetch(API + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null;
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

// --- Tabs ---
$$('.tab').forEach((tab) => {
  tab.addEventListener('click', () => switchTab(tab.dataset.tab));
});

$$('[data-tab-link]').forEach((link) => {
  link.addEventListener('click', (e) => {
    e.preventDefault();
    switchTab(link.dataset.tabLink);
  });
});

function switchTab(name) {
  $$('.tab').forEach((t) => t.classList.toggle('active', t.dataset.tab === name));
  $$('.panel').forEach((p) => p.classList.remove('active'));
  $(`#${name}`).classList.add('active');
  refreshAll();
  if (name === 'analytics' && typeof loadAnalyticsDashboard === 'function') {
    loadAnalyticsDashboard().catch((err) => showToast(err.message, 'error'));
  }
}

$('#report-month').addEventListener('change', refreshAll);

// --- Settings ---
async function loadSettings() {
  userSettings = await api('/settings');
  $('#user-name').textContent = userSettings.userName || 'User';
  $('#user-avatar').textContent = (userSettings.userName || 'U').charAt(0).toUpperCase();
  $('#settings-name').value = userSettings.userName || '';
  $('#settings-income').value = userSettings.monthlyIncome || '';
  updateIncomeMeta();
}

function updateIncomeMeta() {
  const el = $('#stat-income-meta');
  if (userSettings.monthlyIncome > 0) {
    el.textContent = `Income: ${formatMoney(userSettings.monthlyIncome)}/mo`;
  } else {
    el.textContent = 'Set income in settings for % insights';
  }
}

$('#settings-btn').addEventListener('click', () => {
  $('#settings-modal').classList.remove('hidden');
});

$('#settings-cancel').addEventListener('click', () => {
  $('#settings-modal').classList.add('hidden');
});

$('.modal-backdrop').addEventListener('click', () => {
  $('#settings-modal').classList.add('hidden');
});

$('#settings-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    userSettings = await api('/settings', {
      method: 'PUT',
      body: JSON.stringify({
        userName: $('#settings-name').value.trim(),
        monthlyIncome: parseFloat($('#settings-income').value) || 0,
      }),
    });
    $('#user-name').textContent = userSettings.userName || 'User';
    $('#user-avatar').textContent = (userSettings.userName || 'U').charAt(0).toUpperCase();
    $('#settings-modal').classList.add('hidden');
    showToast('Settings saved');
    await refreshAll();
  } catch (err) {
    showToast(err.message, 'error');
  }
});

// --- Categories ---
async function loadCategories() {
  categories = await api('/categories');
  populateCategorySelects();
  renderCategoryList();
}

function populateCategorySelects() {
  const opts = categories.map((c) => `<option value="${c.id}">${c.name}</option>`).join('');
  $('#expense-category').innerHTML = opts;
  $('#budget-category').innerHTML = opts;
}

function renderCategoryList() {
  const el = $('#category-list');
  if (!categories.length) {
    el.innerHTML = '<p class="empty-state">No categories yet.</p>';
    return;
  }
  el.innerHTML = categories.map((c) => `
    <span class="tag">${c.name}
      <button type="button" data-id="${c.id}" title="Delete">&times;</button>
    </span>
  `).join('');
  el.querySelectorAll('button').forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (!confirm('Delete this category?')) return;
      try {
        await api(`/categories/${btn.dataset.id}`, { method: 'DELETE' });
        showToast('Category deleted');
        await refreshAll();
      } catch (e) { showToast(e.message, 'error'); }
    });
  });
}

$('#category-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    await api('/categories', { method: 'POST', body: JSON.stringify({ name: $('#category-name').value.trim() }) });
    $('#category-name').value = '';
    showToast('Category added');
    await refreshAll();
  } catch (err) { showToast(err.message, 'error'); }
});

// --- Expenses ---
async function loadExpenses() {
  const filterMonth = $('#filter-month')?.checked;
  const path = filterMonth ? `/expenses?month=${currentMonth()}` : '/expenses';
  const expenses = await api(path);
  renderExpenseList(expenses);
  return expenses;
}

function renderExpenseList(expenses) {
  const el = $('#expense-list');
  if (!expenses.length) {
    el.innerHTML = '<p class="empty-state">No expenses recorded.</p>';
    return;
  }
  el.innerHTML = `
    <table>
      <thead><tr><th>Date</th><th>Category</th><th>Description</th><th>Amount</th><th></th></tr></thead>
      <tbody>${expenses.map((e) => `
        <tr>
          <td>${e.date}</td><td>${e.categoryName}</td>
          <td>${e.description || '—'}</td>
          <td class="amount">${formatMoney(e.amount)}</td>
          <td><button class="btn btn-danger" data-id="${e.id}">Delete</button></td>
        </tr>`).join('')}
      </tbody>
    </table>`;
  el.querySelectorAll('.btn-danger').forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (!confirm('Delete this expense?')) return;
      try {
        await api(`/expenses/${btn.dataset.id}`, { method: 'DELETE' });
        showToast('Expense deleted');
        await refreshAll();
      } catch (err) { showToast(err.message, 'error'); }
    });
  });
}

function renderRecentTransactions(expenses) {
  const el = $('#recent-transactions');
  const recent = expenses.slice(0, 5);
  if (!recent.length) {
    el.innerHTML = '<p class="empty-state">No transactions this month.</p>';
    return;
  }
  el.innerHTML = recent.map((e) => `
    <div class="tx-item">
      <div class="tx-left">
        <div class="tx-icon">${CATEGORY_ICONS[e.categoryName] || '📦'}</div>
        <div>
          <div class="tx-category">${e.categoryName}</div>
          <div class="tx-desc">${e.description || 'No description'}</div>
        </div>
      </div>
      <div>
        <div class="tx-amount">${formatMoney(e.amount)}</div>
        <div class="tx-date">${e.date}</div>
      </div>
    </div>
  `).join('');
}

$('#expense-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    await api('/expenses', {
      method: 'POST',
      body: JSON.stringify({
        amount: parseFloat($('#expense-amount').value),
        categoryId: $('#expense-category').value,
        date: $('#expense-date').value,
        description: $('#expense-description').value,
      }),
    });
    $('#expense-form').reset();
    setDefaultMonth();
    showToast('Expense added');
    await refreshAll();
  } catch (err) { showToast(err.message, 'error'); }
});

$('#filter-month')?.addEventListener('change', loadExpenses);

// --- Budgets ---
async function loadBudgets() {
  const budgets = await api('/budgets');
  renderBudgetList(budgets);
}

function renderBudgetList(budgets) {
  const el = $('#budget-list');
  if (!budgets.length) {
    el.innerHTML = '<p class="empty-state">No budgets set.</p>';
    return;
  }
  el.innerHTML = `
    <table>
      <thead><tr><th>Category</th><th>Limit</th><th></th></tr></thead>
      <tbody>${budgets.map((b) => `
        <tr>
          <td>${b.categoryName}</td>
          <td class="amount">${formatMoney(b.monthlyLimit)}</td>
          <td><button class="btn btn-danger" data-id="${b.categoryId}">Remove</button></td>
        </tr>`).join('')}
      </tbody>
    </table>`;
  el.querySelectorAll('.btn-danger').forEach((btn) => {
    btn.addEventListener('click', async () => {
      if (!confirm('Remove this budget?')) return;
      try {
        await api(`/budgets/${btn.dataset.id}`, { method: 'DELETE' });
        showToast('Budget removed');
        await refreshAll();
      } catch (err) { showToast(err.message, 'error'); }
    });
  });
}

function renderBudgetStatus(statuses) {
  const el = $('#budget-status');
  if (!statuses.length) {
    el.innerHTML = '<p class="empty-state">No budgets set.</p>';
    return;
  }
  el.innerHTML = statuses.map((s) => {
    const pct = Math.min(s.percentUsed, 100);
    return `
      <div class="budget-item">
        <div class="budget-row">
          <span>${s.categoryName}</span>
          <span class="amount">${formatMoney(s.spent)} / ${formatMoney(s.limit)}</span>
        </div>
        <div class="budget-bar">
          <div class="budget-fill ${s.overBudget ? 'over' : ''}" style="width:${pct}%"></div>
        </div>
        <div class="budget-meta ${s.overBudget ? 'over' : ''}">
          ${s.overBudget ? 'Over budget!' : `${formatMoney(s.remaining)} remaining`} (${Math.round(s.percentUsed)}%)
        </div>
      </div>`;
  }).join('');
}

$('#budget-form').addEventListener('submit', async (e) => {
  e.preventDefault();
  try {
    await api('/budgets', {
      method: 'POST',
      body: JSON.stringify({
        categoryId: $('#budget-category').value,
        monthlyLimit: parseFloat($('#budget-limit').value),
      }),
    });
    $('#budget-limit').value = '';
    showToast('Budget saved');
    await refreshAll();
  } catch (err) { showToast(err.message, 'error'); }
});

// --- Dashboard & Insights ---
async function loadDashboard() {
  const month = currentMonth();
  const [insights, budgetStatus, expenses] = await Promise.all([
    api(`/insights?month=${month}`),
    api(`/budgets/status?month=${month}`),
    api(`/expenses?month=${month}`),
  ]);

  // Stats
  $('#stat-total').textContent = formatMoney(insights.totalSpent);
  $('#stat-count').textContent = expenses.length;
  $('#stat-budget').textContent = formatMoney(insights.totalBudget);

  const util = insights.budgetUtilizationPercent || 0;
  $('#budget-util-bar').style.width = `${Math.min(util, 100)}%`;
  $('#stat-budget-meta').textContent = `${Math.round(util)}% utilized`;

  const changeEl = $('#stat-change-meta');
  if (insights.previousMonthTotal === 0 && insights.totalSpent === 0) {
    changeEl.textContent = 'No prior data';
    changeEl.className = 'stat-meta';
  } else {
    const up = insights.changeFromPreviousMonth >= 0;
    changeEl.textContent = `${up ? '+' : ''}${formatMoney(insights.changeFromPreviousMonth)} vs last month`;
    changeEl.className = `stat-meta ${up ? 'up' : 'down'}`;
  }

  renderInsights(insights);
  renderCategoryBreakdown(insights.categoryBreakdown || []);
  renderTrendChart(insights.trendData || []);
  renderMonthlyReport(insights);
  renderRecentTransactions(expenses);
  renderBudgetStatus(budgetStatus);
}

function renderInsights(insights) {
  const list = $('#insights-list');
  const items = (insights.insights || []).filter((i) => i.type !== 'SUMMARY');

  if (!items.length) {
    list.innerHTML = '<p class="empty-state">Add expenses to unlock smart insights.</p>';
    $('#savings-badge').classList.remove('visible');
    return;
  }

  list.innerHTML = items.map((i) => {
    const cls = i.severity === 'WARNING' ? 'warning'
      : i.severity === 'SUCCESS' ? 'success' : '';
    return `
      <div class="insight-item ${cls}">
        <span class="insight-bullet">•</span>
        <span>${i.message}</span>
      </div>`;
  }).join('');

  const badge = $('#savings-badge');
  if (insights.potentialMonthlySavings > 0) {
    badge.textContent = `💰 Potential monthly savings: ${formatMoney(insights.potentialMonthlySavings)}`;
    badge.classList.add('visible');
  } else {
    badge.classList.remove('visible');
  }
}

function renderCategoryBreakdown(breakdown) {
  const el = $('#category-breakdown');
  if (!breakdown.length) {
    el.innerHTML = '<p class="empty-state">No spending this month.</p>';
    return;
  }
  const max = Math.max(...breakdown.map((s) => s.currentMonth));
  el.innerHTML = breakdown
    .sort((a, b) => b.currentMonth - a.currentMonth)
    .map((s) => {
      const pct = max > 0 ? (s.currentMonth / max) * 100 : 0;
      return `
        <div class="breakdown-item">
          <div class="breakdown-row">
            <span>${s.categoryName}</span>
            <span class="amount">${formatMoney(s.currentMonth)}</span>
          </div>
          <div class="breakdown-bar">
            <div class="breakdown-fill" style="width:${pct}%"></div>
          </div>
        </div>`;
    }).join('');
}

function renderTrendChart(trendData) {
  const el = $('#trend-chart');
  if (!trendData.length) {
    el.innerHTML = '<p class="empty-state">Not enough data for trends.</p>';
    return;
  }
  const max = Math.max(...trendData.map((t) => t.totalSpent), 1);
  el.innerHTML = trendData.map((t) => {
    const pct = (t.totalSpent / max) * 100;
    return `
      <div class="trend-bar-wrap">
        <div class="trend-value">${t.totalSpent > 0 ? formatMoney(t.totalSpent) : '—'}</div>
        <div class="trend-bar" style="height:${Math.max(pct, 3)}%"></div>
        <div class="trend-label">${t.month}</div>
      </div>`;
  }).join('');
}

function renderMonthlyReport(insights) {
  $('#monthly-summary').textContent = insights.summary || 'No data available.';

  const chips = [];
  if (insights.highestCategory) chips.push(`Top: ${insights.highestCategory}`);
  if (insights.fastestGrowingCategory) chips.push(`Fastest growing: ${insights.fastestGrowingCategory}`);
  if (insights.lowestCategory) chips.push(`Lowest: ${insights.lowestCategory}`);
  if (insights.changePercent !== undefined) {
    const dir = insights.changePercent >= 0 ? '↑' : '↓';
    chips.push(`MoM ${dir} ${Math.abs(Math.round(insights.changePercent))}%`);
  }

  $('#report-highlights').innerHTML = chips
    .map((c) => `<span class="highlight-chip">${c}</span>`)
    .join('');
}

async function refreshAll() {
  try {
    await loadSettings();
    await loadCategories();
    const activeTab = document.querySelector('.tab.active')?.dataset.tab;
    const tasks = [loadDashboard(), loadExpenses(), loadBudgets()];
    if (activeTab === 'analytics' && typeof loadAnalyticsDashboard === 'function') {
      tasks.push(loadAnalyticsDashboard());
    }
    await Promise.all(tasks);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

setDefaultMonth();
refreshAll();
