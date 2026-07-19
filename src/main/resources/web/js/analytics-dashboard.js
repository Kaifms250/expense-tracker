/**
 * Monthly Analytics Dashboard — renders /api/analytics data
 */

const CHART_COLORS = ['#6c5ce7', '#00d2a0', '#fdcb6e', '#ff6b6b', '#a29bfe', '#74b9ff', '#e17055', '#636e72'];

function formatMoneyShort(amount) {
  if (amount >= 100000) return '₹' + (amount / 100000).toFixed(1) + 'L';
  if (amount >= 1000) return '₹' + (amount / 1000).toFixed(1) + 'K';
  return '₹' + Number(amount).toFixed(0);
}

function budgetStatusClass(status) {
  if (!status) return '';
  const s = status.toLowerCase();
  if (s === 'excellent') return 'excellent';
  if (s === 'good') return 'good';
  if (s === 'warning') return 'warning';
  if (s === 'critical') return 'critical';
  return '';
}

function setBudgetRing(percent) {
  const ring = document.getElementById('an-budget-ring');
  const label = document.getElementById('an-budget-pct');
  if (!ring) return;
  const circumference = 2 * Math.PI * 34;
  const offset = circumference - (Math.min(percent, 100) / 100) * circumference;
  ring.style.strokeDasharray = circumference;
  ring.style.strokeDashoffset = offset;
  label.textContent = Math.round(percent) + '%';
  ring.className = 'ring-fill';
  if (percent > 100) ring.classList.add('critical');
  else if (percent >= 80) ring.classList.add('warning');
  else if (percent > 0) ring.classList.add('excellent');
}

function renderLineChart(trendData) {
  const el = document.getElementById('an-line-chart');
  if (!el || !trendData.length) {
    if (el) el.innerHTML = '<p class="empty-state">Not enough data for trend chart.</p>';
    return;
  }

  const w = 400, h = 180, padX = 30, padY = 20;
  const max = Math.max(...trendData.map((t) => t.totalSpent), 1);
  const step = (w - padX * 2) / Math.max(trendData.length - 1, 1);

  const points = trendData.map((t, i) => {
    const x = padX + i * step;
    const y = h - padY - (t.totalSpent / max) * (h - padY * 2);
    return { x, y, ...t };
  });

  const linePath = points.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x},${p.y}`).join(' ');
  const areaPath = linePath + ` L${points[points.length - 1].x},${h - padY} L${points[0].x},${h - padY} Z`;

  el.innerHTML = `
    <svg viewBox="0 0 ${w} ${h + 30}" preserveAspectRatio="xMidYMid meet" style="width:100%;height:200px">
      <defs>
        <linearGradient id="lineGradient" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="#a29bfe" stop-opacity="0.4"/>
          <stop offset="100%" stop-color="#a29bfe" stop-opacity="0"/>
        </linearGradient>
      </defs>
      <path class="line-chart-area" d="${areaPath}"/>
      <path class="line-chart-path" d="${linePath}"/>
      ${points.map((p) => `<circle class="line-chart-dot" cx="${p.x}" cy="${p.y}" r="4"/>`).join('')}
      ${points.map((p, i) => `<text x="${p.x}" y="${h + 18}" text-anchor="middle" fill="#8b8fa3" font-size="9">${p.month.split(' ')[0]}</text>`).join('')}
    </svg>`;
}

function renderDoughnut(categories) {
  const chartEl = document.getElementById('an-doughnut');
  const legendEl = document.getElementById('an-doughnut-legend');
  if (!chartEl || !categories.length) {
    if (chartEl) chartEl.innerHTML = '<p class="empty-state">No category data.</p>';
    if (legendEl) legendEl.innerHTML = '';
    return;
  }

  const total = categories.reduce((s, c) => s + c.spent, 0);
  const r = 56, cx = 80, cy = 80, circ = 2 * Math.PI * r;
  let offset = 0;
  let segments = '';

  categories.forEach((cat, i) => {
    const pct = total > 0 ? cat.spent / total : 0;
    const len = pct * circ;
    const color = CHART_COLORS[i % CHART_COLORS.length];
    segments += `<circle class="doughnut-segment" cx="${cx}" cy="${cy}" r="${r}"
      stroke="${color}" stroke-dasharray="${len} ${circ - len}" stroke-dashoffset="${-offset}"/>`;
    offset += len;
  });

  chartEl.innerHTML = `<svg viewBox="0 0 160 160"><g transform="rotate(-90 80 80)">${segments}</g>
    <text x="80" y="76" text-anchor="middle" fill="currentColor" font-size="11" font-weight="600">Total</text>
    <text x="80" y="92" text-anchor="middle" fill="currentColor" font-size="10">${formatMoneyShort(total)}</text>
  </svg>`;

  legendEl.innerHTML = categories.map((cat, i) => `
    <div class="legend-item">
      <span class="legend-dot" style="background:${CHART_COLORS[i % CHART_COLORS.length]}"></span>
      <span>${cat.categoryName}</span>
      <span class="legend-pct">${Math.round(cat.percentOfTotal)}%</span>
    </div>`).join('');
}

function renderTopCategories(categories) {
  const el = document.getElementById('an-top-categories');
  if (!el) return;
  if (!categories.length) {
    el.innerHTML = '<p class="empty-state">No category spending this month.</p>';
    return;
  }
  const max = Math.max(...categories.map((c) => c.spent));
  el.innerHTML = categories.map((cat, i) => {
    const pct = max > 0 ? (cat.spent / max) * 100 : 0;
    return `
      <div class="top-cat-item">
        <div class="top-cat-row">
          <span><span class="top-cat-rank">${i + 1}</span>${cat.categoryName}</span>
          <span class="amount">${formatMoney(cat.spent)} <small style="color:var(--text-muted)">(${Math.round(cat.percentOfTotal)}%)</small></span>
        </div>
        <div class="top-cat-bar"><div class="top-cat-fill" style="width:${pct}%"></div></div>
      </div>`;
  }).join('');
}

function renderBudgetBars(categories) {
  const el = document.getElementById('an-budget-bars');
  if (!el) return;
  const withBudget = categories.filter((c) => c.budgetLimit > 0);
  if (!withBudget.length) {
    el.innerHTML = '<p class="empty-state">No budgets set for categories.</p>';
    return;
  }
  el.innerHTML = withBudget.map((cat) => {
    const pct = Math.min(cat.budgetUtilizationPercent, 100);
    const over = cat.budgetUtilizationPercent > 100;
    return `
      <div class="budget-bar-item">
        <div class="budget-row">
          <span>${cat.categoryName}</span>
          <span class="amount">${formatMoney(cat.spent)} / ${formatMoney(cat.budgetLimit)}</span>
        </div>
        <div class="budget-bar">
          <div class="budget-fill ${over ? 'over' : ''}" style="width:${pct}%"></div>
        </div>
        <div class="budget-meta ${over ? 'over' : ''}">
          ${over ? 'Over budget!' : `${formatMoney(cat.remainingBudget)} remaining`} (${Math.round(cat.budgetUtilizationPercent)}%)
        </div>
      </div>`;
  }).join('');
}

function renderStatistics(stats) {
  const el = document.getElementById('an-statistics');
  if (!el) return;
  const items = [
    ['Highest Expense', formatMoney(stats.highestExpense || 0)],
    ['Lowest Expense', formatMoney(stats.lowestExpense || 0)],
    ['Largest Transaction', formatMoney(stats.largestTransaction || 0)],
    ['Daily Average', formatMoney(stats.dailyAverageSpending || 0)],
    ['Weekly Average', formatMoney(stats.weeklyAverageSpending || 0)],
    ['Monthly Average', formatMoney(stats.monthlyAverageSpending || 0)],
  ];
  el.innerHTML = items.map(([label, value]) => `
    <div class="stat-item-an">
      <span class="stat-label">${label}</span>
      <span class="stat-value">${value}</span>
    </div>`).join('');
}

function renderRecentAnalytics(stats, data) {
  const el = document.getElementById('an-recent');
  if (!el) return;
  const items = [
    ['Total Transactions', stats.totalTransactions || 0],
    ['Most Active Day', stats.mostActiveSpendingDay || '—'],
    ['Avg Transaction', formatMoney(stats.averageExpensePerTransaction || 0)],
    ['Most Frequent Category', data.mostFrequentCategory || '—'],
    ['Highest Category', data.highestSpendingCategory || '—'],
    ['Remaining Budget', formatMoney(data.remainingBudget || 0)],
  ];
  el.innerHTML = items.map(([label, value]) => `
    <div class="stat-item-an">
      <span class="stat-label">${label}</span>
      <span class="stat-value">${value}</span>
    </div>`).join('');
}

function renderSmartInsights(insights) {
  const el = document.getElementById('an-smart-insights');
  if (!el) return;
  const filtered = (insights || []).filter((i) => i.type !== 'SUMMARY');
  if (!filtered.length) {
    el.innerHTML = '<p class="empty-state">Add expenses to generate insights.</p>';
    return;
  }
  el.innerHTML = filtered.map((i) => {
    const cls = i.severity === 'WARNING' ? 'warning' : i.severity === 'SUCCESS' ? 'success' : '';
    return `<div class="insight-row-an ${cls}"><span>•</span><span>${i.message}</span></div>`;
  }).join('');
}

function renderRecommendations(recs) {
  const el = document.getElementById('an-recommendations');
  if (!el) return;
  if (!recs || !recs.length) {
    el.innerHTML = '<p class="empty-state">No recommendations yet.</p>';
    return;
  }
  el.innerHTML = recs.map((r) => `
    <div class="rec-item"><span class="rec-icon">✦</span><span>${r}</span></div>`).join('');
}

async function loadAnalyticsDashboard() {
  const month = typeof currentMonth === 'function' ? currentMonth() : new Date().toISOString().slice(0, 7);
  const data = await api(`/analytics?month=${month}`);
  const stats = data.statistics || {};

  // KPI Cards
  document.getElementById('an-total-expenses').textContent = formatMoney(data.totalExpenses || 0);
  document.getElementById('an-total-savings').textContent = formatMoney(data.totalSavings || 0);
  document.getElementById('an-net-balance').textContent = formatMoney(data.netBalance || 0);
  document.getElementById('an-savings-rate').textContent =
    (data.totalIncome > 0 ? Math.round(data.savingsRatePercent) + '% of income' : 'Set income in settings');
  document.getElementById('an-income-label').textContent =
    'Income: ' + (data.totalIncome > 0 ? formatMoney(data.totalIncome) : 'Not set');

  const netEl = document.getElementById('an-net-balance');
  netEl.className = 'kpi-value' + (data.netBalance < 0 ? ' danger' : data.netBalance > 0 ? ' success' : '');

  const trendEl = document.getElementById('an-expense-trend');
  if (data.previousMonthTotal > 0 || data.totalExpenses > 0) {
    const up = data.changeFromPreviousMonth >= 0;
    trendEl.textContent = `${up ? '▲' : '▼'} ${formatMoney(Math.abs(data.changeFromPreviousMonth))} vs last month`;
    trendEl.className = `kpi-trend ${up ? 'up' : 'down'}`;
  } else {
    trendEl.textContent = '—';
    trendEl.className = 'kpi-trend';
  }

  setBudgetRing(data.budgetUtilizationPercent || 0);
  const statusEl = document.getElementById('an-budget-status');
  statusEl.textContent = data.budgetStatus || '—';
  statusEl.className = 'status-badge ' + budgetStatusClass(data.budgetStatus);

  renderLineChart(data.monthlyTrend || []);
  renderDoughnut(data.allCategories || []);
  renderTopCategories(data.topCategories || []);
  renderBudgetBars(data.allCategories || []);
  renderStatistics(stats);
  renderRecentAnalytics(stats, data);
  renderSmartInsights(data.smartInsights || []);
  renderRecommendations(data.recommendations || []);
}

// Theme toggle
(function initTheme() {
  const btn = document.getElementById('theme-toggle');
  if (!btn) return;
  const saved = localStorage.getItem('theme');
  if (saved === 'light') {
    document.body.classList.add('light-mode');
    btn.textContent = '☀️';
  }
  btn.addEventListener('click', () => {
    document.body.classList.toggle('light-mode');
    const isLight = document.body.classList.contains('light-mode');
    btn.textContent = isLight ? '☀️' : '🌙';
    localStorage.setItem('theme', isLight ? 'light' : 'dark');
  });
})();

// Hook into tab switch
document.addEventListener('DOMContentLoaded', () => {
  const origSwitch = typeof switchTab === 'function' ? switchTab : null;
});

// Expose for app.js
window.loadAnalyticsDashboard = loadAnalyticsDashboard;
