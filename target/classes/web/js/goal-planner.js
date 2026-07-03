// ── Goal-Based Savings Planner ─────────────────────────────────────────────
// Handles all goal-related UI: fetching, rendering, creating, depositing,
// updating and deleting savings goals via /api/goals endpoints.

const GOAL_API = '/api/goals';

// ── Helpers ────────────────────────────────────────────────────────────────

function formatGoalMoney(amount) {
  return '₹' + Number(amount).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function goalStatusClass(status) {
  if (!status) return 'on-track';
  const s = status.toString().toUpperCase();
  if (s === 'ACHIEVED') return 'achieved';
  if (s === 'BEHIND_SCHEDULE') return 'behind-schedule';
  return 'on-track';
}

function goalStatusLabel(status) {
  if (!status) return 'On Track';
  const s = status.toString().toUpperCase();
  if (s === 'ACHIEVED') return '✅ Achieved';
  if (s === 'BEHIND_SCHEDULE') return '⚠️ Behind Schedule';
  return '✅ On Track';
}

function goalProgressColor(status) {
  if (!status) return '#3B82F6';
  const s = status.toString().toUpperCase();
  if (s === 'ACHIEVED') return '#22C55E';
  if (s === 'BEHIND_SCHEDULE') return '#F97316';
  return '#3B82F6';
}

async function goalApi(path, options = {}) {
  const res = await fetch(GOAL_API + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (res.status === 204) return null;
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || 'Request failed');
  return data;
}

// ── Fetch ───────────────────────────────────────────────────────────────────

async function fetchAllGoals() {
  return await goalApi('');
}

// ── Render Summary Card ─────────────────────────────────────────────────────

function renderGoalSummary(goals) {
  const container = document.querySelector('#goal-summary-card');
  if (!container) return;

  const active = goals.filter(g => {
    const s = (g.status || '').toUpperCase();
    return s !== 'ACHIEVED';
  });
  const totalTarget = goals.reduce((s, g) => s + (g.targetAmount || 0), 0);
  const totalSaved  = goals.reduce((s, g) => s + (g.currentSavings || 0), 0);
  const overall     = totalTarget > 0 ? Math.round((totalSaved / totalTarget) * 100) : 0;

  container.innerHTML = `
    <div class="goal-summary-grid">
      <div class="goal-summary-stat">
        <span class="goal-summary-value">${active.length}</span>
        <span class="goal-summary-label">Active Goals</span>
      </div>
      <div class="goal-summary-stat">
        <span class="goal-summary-value">${formatGoalMoney(totalTarget)}</span>
        <span class="goal-summary-label">Total Target</span>
      </div>
      <div class="goal-summary-stat">
        <span class="goal-summary-value">${formatGoalMoney(totalSaved)}</span>
        <span class="goal-summary-label">Total Saved</span>
      </div>
      <div class="goal-summary-stat">
        <span class="goal-summary-value">${overall}%</span>
        <span class="goal-summary-label">Overall Progress</span>
      </div>
    </div>
    <div class="goal-overall-bar-wrap">
      <div class="goal-overall-bar-track">
        <div class="goal-overall-bar-fill" style="width:${Math.min(overall,100)}%; background:${overall >= 100 ? '#22C55E' : '#6c5ce7'}"></div>
      </div>
      <span class="goal-overall-bar-pct">${overall}%</span>
    </div>
  `;
}

// ── Render Goal Cards ───────────────────────────────────────────────────────

function renderGoalCards(goals) {
  const grid    = document.querySelector('#goal-cards-grid');
  const archive = document.querySelector('#goal-cards-achieved');
  if (!grid || !archive) return;

  const active   = goals.filter(g => (g.status || '').toUpperCase() !== 'ACHIEVED');
  const achieved = goals.filter(g => (g.status || '').toUpperCase() === 'ACHIEVED');

  // Empty state
  const emptyEl = document.querySelector('#goal-empty-state');
  if (emptyEl) emptyEl.style.display = active.length === 0 ? 'block' : 'none';

  grid.innerHTML = active.map(goalCardHTML).join('');
  archive.innerHTML = achieved.map(goalCardHTML).join('');

  // Attach event listeners
  grid.querySelectorAll('.goal-view-btn').forEach(btn => {
    btn.addEventListener('click', () => openGoalModal(btn.dataset.id, goals));
  });
  archive.querySelectorAll('.goal-view-btn').forEach(btn => {
    btn.addEventListener('click', () => openGoalModal(btn.dataset.id, goals));
  });
}

function goalCardHTML(g) {
  const pct        = Math.min(Math.max(g.progressPercent || 0, 0), 100);
  const statusCls  = goalStatusClass(g.status);
  const statusLbl  = goalStatusLabel(g.status);
  const color      = goalProgressColor(g.status);
  const nameShort  = (g.name || '').length > 30 ? g.name.substring(0, 30) + '…' : (g.name || '');
  const monthly    = (g.requiredMonthlySavings > 0)
    ? formatGoalMoney(g.requiredMonthlySavings) + '/mo'
    : 'N/A';
  const days       = (g.daysRemaining > 0) ? g.daysRemaining + ' days left' : 'Deadline passed';

  return `
    <div class="goal-card glass">
      <div class="goal-card-header">
        <span class="goal-card-icon">${g.icon || '💰'}</span>
        <div class="goal-card-title-wrap">
          <h4 class="goal-card-title" title="${g.name || ''}">${nameShort}</h4>
          <span class="goal-status-badge ${statusCls}">${statusLbl}</span>
        </div>
      </div>
      <div class="goal-progress-bar">
        <div class="goal-progress-fill" style="width:${pct}%; background:${color}"></div>
      </div>
      <div class="goal-card-amounts">
        <span class="goal-saved">${formatGoalMoney(g.currentSavings || 0)}</span>
        <span class="goal-divider">/</span>
        <span class="goal-target">${formatGoalMoney(g.targetAmount || 0)}</span>
        <span class="goal-pct">${Math.round(pct)}%</span>
      </div>
      <div class="goal-card-meta">
        <span>📅 ${days}</span>
        <span>💳 ${monthly}</span>
      </div>
      <button class="btn goal-view-btn" data-id="${g.id}">View Details</button>
    </div>
  `;
}

// ── Goal Detail Modal ───────────────────────────────────────────────────────

function openGoalModal(id, goals) {
  const g = goals.find(x => x.id === id);
  if (!g) return;

  const modal = document.querySelector('#goal-modal');
  const body  = document.querySelector('#goal-modal-body');
  if (!modal || !body) return;

  const pct    = Math.min(Math.max(g.progressPercent || 0, 0), 100);
  const color  = goalProgressColor(g.status);
  const statusCls = goalStatusClass(g.status);
  const statusLbl = goalStatusLabel(g.status);

  body.innerHTML = `
    <div class="goal-modal-icon">${g.icon || '💰'}</div>
    <h3 class="goal-modal-name">${g.name || ''}</h3>
    <span class="goal-status-badge ${statusCls} goal-modal-badge">${statusLbl}</span>

    <div class="goal-modal-progress">
      <div class="goal-progress-bar goal-modal-bar">
        <div class="goal-progress-fill" style="width:${pct}%; background:${color}"></div>
      </div>
      <span>${Math.round(pct)}%</span>
    </div>

    <div class="goal-modal-stats">
      <div class="goal-modal-stat">
        <span class="goal-modal-stat-label">Saved</span>
        <span class="goal-modal-stat-value">${formatGoalMoney(g.currentSavings || 0)}</span>
      </div>
      <div class="goal-modal-stat">
        <span class="goal-modal-stat-label">Target</span>
        <span class="goal-modal-stat-value">${formatGoalMoney(g.targetAmount || 0)}</span>
      </div>
      <div class="goal-modal-stat">
        <span class="goal-modal-stat-label">Remaining</span>
        <span class="goal-modal-stat-value">${formatGoalMoney(g.remainingAmount || 0)}</span>
      </div>
      <div class="goal-modal-stat">
        <span class="goal-modal-stat-label">Monthly Need</span>
        <span class="goal-modal-stat-value">${g.requiredMonthlySavings > 0 ? formatGoalMoney(g.requiredMonthlySavings) : 'N/A'}</span>
      </div>
      <div class="goal-modal-stat">
        <span class="goal-modal-stat-label">Daily Target</span>
        <span class="goal-modal-stat-value">${g.dailySavingsTarget > 0 ? formatGoalMoney(g.dailySavingsTarget) : 'N/A'}</span>
      </div>
      <div class="goal-modal-stat">
        <span class="goal-modal-stat-label">Days Left</span>
        <span class="goal-modal-stat-value">${g.daysRemaining > 0 ? g.daysRemaining : '0'}</span>
      </div>
    </div>

    <div class="goal-modal-actions">
      <button class="btn btn-primary" id="goal-deposit-btn" data-id="${g.id}">+ Add Savings</button>
      <button class="btn btn-ghost" id="goal-delete-btn" data-id="${g.id}">Delete Goal</button>
    </div>
  `;

  modal.classList.remove('hidden');

  document.querySelector('#goal-deposit-btn').addEventListener('click', async () => {
    const amt = parseFloat(prompt('Enter amount to add (₹):'));
    if (!amt || amt <= 0) return;
    try {
      await goalApi(`/${id}/deposit`, {
        method: 'POST',
        body: JSON.stringify({ amount: amt }),
      });
      modal.classList.add('hidden');
      showToast('Savings added!');
      await loadGoals();
    } catch (err) { showToast(err.message, 'error'); }
  });

  document.querySelector('#goal-delete-btn').addEventListener('click', async () => {
    if (!confirm(`Delete goal "${g.name}"?`)) return;
    try {
      await goalApi(`/${id}`, { method: 'DELETE' });
      modal.classList.add('hidden');
      showToast('Goal deleted');
      await loadGoals();
    } catch (err) { showToast(err.message, 'error'); }
  });
}

// ── Create Goal Form ────────────────────────────────────────────────────────

function initGoalForm() {
  const form = document.querySelector('#goal-create-form');
  if (!form) return;

  // Toggle date vs duration
  const dateRadio     = document.querySelector('#goal-by-date');
  const durationRadio = document.querySelector('#goal-by-duration');
  const dateField     = document.querySelector('#goal-date-field');
  const durField      = document.querySelector('#goal-duration-field');

  function toggleDeadlineField() {
    if (dateRadio && dateRadio.checked) {
      if (dateField)    dateField.style.display    = 'block';
      if (durField)     durField.style.display     = 'none';
    } else {
      if (dateField)    dateField.style.display    = 'none';
      if (durField)     durField.style.display     = 'block';
    }
  }

  if (dateRadio)     dateRadio.addEventListener('change', toggleDeadlineField);
  if (durationRadio) durationRadio.addEventListener('change', toggleDeadlineField);
  toggleDeadlineField();

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
      const name           = document.querySelector('#goal-name').value.trim();
      const targetAmount   = parseFloat(document.querySelector('#goal-target-amount').value);
      const currentSavings = parseFloat(document.querySelector('#goal-current-savings').value) || 0;
      const useDate        = dateRadio && dateRadio.checked;
      const targetDate     = useDate ? document.querySelector('#goal-target-date').value : null;
      const durationMonths = !useDate
        ? parseInt(document.querySelector('#goal-duration-months').value, 10)
        : null;

      const payload = { name, targetAmount, currentSavings };
      if (targetDate)     payload.targetDate           = targetDate;
      if (durationMonths) payload.targetDurationMonths = durationMonths;

      await goalApi('', { method: 'POST', body: JSON.stringify(payload) });
      form.reset();
      toggleDeadlineField();
      showToast('Goal created!');
      await loadGoals();

      // Collapse create form
      const createSection = document.querySelector('#goal-create-section');
      if (createSection) createSection.classList.add('hidden');
    } catch (err) { showToast(err.message, 'error'); }
  });

  // Toggle create form visibility
  const addBtn = document.querySelector('#goal-add-btn');
  if (addBtn) {
    addBtn.addEventListener('click', () => {
      const s = document.querySelector('#goal-create-section');
      if (s) s.classList.toggle('hidden');
    });
  }
}

// ── Achieved Toggle ─────────────────────────────────────────────────────────

function initAchievedToggle() {
  const toggle  = document.querySelector('#goal-achieved-toggle');
  const section = document.querySelector('#goal-achieved-section');
  if (!toggle || !section) return;

  toggle.addEventListener('click', () => {
    const hidden = section.classList.toggle('hidden');
    toggle.textContent = hidden ? 'Show Completed Goals ▼' : 'Hide Completed Goals ▲';
  });
}

// ── Modal Close ─────────────────────────────────────────────────────────────

function initGoalModalClose() {
  const backdrop = document.querySelector('#goal-modal-backdrop');
  const closeBtn = document.querySelector('#goal-modal-close');
  const modal    = document.querySelector('#goal-modal');
  if (backdrop) backdrop.addEventListener('click', () => modal && modal.classList.add('hidden'));
  if (closeBtn)  closeBtn.addEventListener('click',  () => modal && modal.classList.add('hidden'));
}

// ── Main Load ───────────────────────────────────────────────────────────────

async function loadGoals() {
  try {
    const goals = await fetchAllGoals();
    renderGoalSummary(goals);
    renderGoalCards(goals);
  } catch (err) {
    const grid = document.querySelector('#goal-cards-grid');
    if (grid) grid.innerHTML = '<p class="empty-state">Could not load goals.</p>';
  }
}

// ── Init ────────────────────────────────────────────────────────────────────

function initGoalPlanner() {
  initGoalForm();
  initAchievedToggle();
  initGoalModalClose();
  loadGoals();
}

// Run after DOM is ready (app.js calls refreshAll on DOMContentLoaded-equivalent)
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initGoalPlanner);
} else {
  initGoalPlanner();
}
