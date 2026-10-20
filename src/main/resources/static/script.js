'use strict';

const button = document.querySelector('#process-button');
const search = document.querySelector('#search');
const rows = document.querySelector('#employee-rows');
const empty = document.querySelector('#empty-state');
const statusMessage = document.querySelector('#processing-status');
const error = document.querySelector('#error');
const salaryFormat = new Intl.NumberFormat('en-US', { minimumFractionDigits: 3, maximumFractionDigits: 3 });
let employees = [];

// Use textContent for CSV values so names cannot inject HTML into the page.
function addCell(row, value, className = '') {
  const cell = document.createElement('td');
  cell.textContent = value;
  cell.className = className;
  row.append(cell);
  return cell;
}

function renderEmployees() {
  const query = search.value.trim().toLocaleLowerCase();
  const filtered = employees.filter(employee => employee.name.toLocaleLowerCase().includes(query));
  rows.replaceChildren();
  for (const employee of filtered) {
    const row = document.createElement('tr');
    addCell(row, String(employee.id).padStart(3, '0'));
    addCell(row, employee.name);
    addCell(row, employee.role);
    addCell(row, salaryFormat.format(employee.originalSalary), 'numeric');
    addCell(row, salaryFormat.format(employee.newSalary), 'numeric');
    addCell(row, `${Number(employee.increasePercentage).toFixed(3)}%`, 'numeric');
    const completion = addCell(row, '');
    const wrapper = document.createElement('div');
    wrapper.className = 'completion';
    const percent = Math.round(employee.projectCompletion * 100);
    const progress = document.createElement('progress');
    progress.max = 100;
    progress.value = percent;
    progress.setAttribute('aria-label', `${employee.name}: ${percent}% project completion`);
    const label = document.createElement('span');
    label.textContent = `${percent}%`;
    wrapper.append(progress, label);
    completion.append(wrapper);
    const badge = document.createElement('span');
    const statusClass = employee.status === 'Bonus Applied' ? 'bonus' : employee.status === 'Increased' ? 'increase' : 'none';
    badge.className = `badge ${statusClass}`;
    badge.textContent = employee.status;
    addCell(row, '').append(badge);
    rows.append(row);
  }
  empty.hidden = filtered.length > 0;
  empty.querySelector('h3').textContent = employees.length ? 'No matching employees' : 'No employees processed yet';
  empty.querySelector('p').textContent = employees.length ? 'Try a different employee name.' : 'Click Process Employees to calculate salary updates and bring your employee data into view.';
  document.querySelector('#visible-count').textContent = `Showing ${filtered.length} of ${employees.length} employees`;
}

async function processEmployees() {
  button.disabled = true;
  error.hidden = true;
  document.querySelector('.results').setAttribute('aria-busy', 'true');
  document.querySelector('#button-icon').classList.add('spinning');
  document.querySelector('#button-label').textContent = 'Processing…';
  statusMessage.textContent = 'Processing employees with Java multithreading. Please wait…';
  try {
    const response = await fetch('/api/employees/process', { method: 'POST', headers: { Accept: 'application/json' } });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Processing failed. Please try again.');
    if (!Array.isArray(data)) throw new Error('The server returned an unexpected response. Please try again.');
    employees = data;
    document.querySelector('#total').textContent = employees.length;
    document.querySelector('#increased').textContent = employees.filter(employee => employee.status !== 'No Increase').length;
    document.querySelector('#bonus').textContent = employees.filter(employee => employee.status === 'Bonus Applied').length;
    document.querySelector('#result-count').textContent = employees.length;
    search.disabled = false;
    renderEmployees();
    statusMessage.textContent = `Processing complete. ${employees.length} employees processed successfully.`;
  } catch (failure) {
    error.textContent = failure instanceof TypeError ? 'Unable to reach the server. Check that the application is running and try again.' : failure.message;
    error.hidden = false;
    statusMessage.textContent = employees.length ? 'Processing failed. The previous successful results are still displayed.' : 'Processing failed. Please try again.';
  } finally {
    button.disabled = false;
    document.querySelector('.results').setAttribute('aria-busy', 'false');
    document.querySelector('#button-icon').classList.remove('spinning');
    document.querySelector('#button-label').textContent = 'Process Employees';
  }
}

button.addEventListener('click', processEmployees);
search.addEventListener('input', renderEmployees);
