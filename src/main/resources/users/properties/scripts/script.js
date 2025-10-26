let currentSort = {
	column: null,
	direction: 'asc'
};

let allUsers = [];

// Initialize users data
document.addEventListener('DOMContentLoaded', function () {
	const rows = document.querySelectorAll('.user-row');
	allUsers = Array.from(rows).map(row => row.cloneNode(true));

	// Add search functionality
	const searchInput = document.getElementById('searchInput');
	searchInput.addEventListener('input', filterUsers);
});

function filterUsers() {
	const searchTerm = document.getElementById('searchInput').value.toLowerCase();
	const rows = document.querySelectorAll('.user-row');
	const tbody = document.querySelector('#usersTable tbody');

	rows.forEach(row => {
		const text = row.textContent.toLowerCase();
		if (text.includes(searchTerm)) {
			row.style.display = '';
		} else {
			row.style.display = 'none';
		}
	});
}

function sortTable(column) {
	const tbody = document.querySelector('#usersTable tbody');
	const rows = Array.from(tbody.querySelectorAll('.user-row'));

	const direction = currentSort.column === column ?
		(currentSort.direction === 'asc' ? 'desc' : 'asc') : 'asc';

	rows.sort((a, b) => {
		let aValue, bValue;

		switch (column) {
			case 'id':
				aValue = parseInt(a.querySelector('.user-id').textContent);
				bValue = parseInt(b.querySelector('.user-id').textContent);
				break;
			case 'firstName':
				aValue = a.querySelector('.first-name').textContent.toLowerCase();
				bValue = b.querySelector('.first-name').textContent.toLowerCase();
				break;
			case 'lastName':
				aValue = a.querySelector('.last-name').textContent.toLowerCase();
				bValue = b.querySelector('.last-name').textContent.toLowerCase();
				break;
			case 'email':
				aValue = a.querySelector('.email').textContent.toLowerCase();
				bValue = b.querySelector('.email').textContent.toLowerCase();
				break;
			default:
				return 0;
		}

		if (aValue < bValue) return direction === 'asc' ? -1 : 1;
		if (aValue > bValue) return direction === 'asc' ? 1 : -1;
		return 0;
	});

	// Clear table
	while (tbody.firstChild) {
		tbody.removeChild(tbody.firstChild);
	}

	// Add sorted rows
	rows.forEach(row => tbody.appendChild(row));

	// Update sort state
	currentSort = { column, direction };

	// Update header indicators
	updateSortIndicators(column, direction);
}

function updateSortIndicators(column, direction) {
	// Remove all indicators
	document.querySelectorAll('th').forEach(th => {
		th.textContent = th.textContent.replace(' ↑', '').replace(' ↓', '');
	});

	// Add indicator to current column
	const currentTh = document.querySelector(`th[data-sort="${column}"]`);
	if (currentTh) {
		currentTh.textContent += direction === 'asc' ? ' ↑' : ' ↓';
	}
}

function viewUser(userId) {
	const row = document.querySelector(`.user-row .user-id:contains("${userId}")`)?.closest('.user-row');
	if (!row) return;

	const userData = {
		id: row.querySelector('.user-id').textContent,
		firstName: row.querySelector('.first-name').textContent,
		lastName: row.querySelector('.last-name').textContent,
		email: row.querySelector('.email').textContent,
		address: row.querySelector('.address').textContent
	};

	const detailsContent = `
        <div class="detail-item">
            <strong>ID:</strong> ${userData.id}
        </div>
        <div class="detail-item">
            <strong>Name:</strong> ${userData.firstName} ${userData.lastName}
        </div>
        <div class="detail-item">
            <strong>Email:</strong> <a href="mailto:${userData.email}">${userData.email}</a>
        </div>
        <div class="detail-item">
            <strong>Address:</strong> ${userData.address}
        </div>
    `;

	document.getElementById('detailsContent').innerHTML = detailsContent;
	document.getElementById('userDetails').style.display = 'block';

	// Highlight the row
	document.querySelectorAll('.user-row').forEach(r => r.classList.remove('highlight'));
	row.classList.add('highlight');

	// Scroll to details
	document.getElementById('userDetails').scrollIntoView({ behavior: 'smooth' });
}

function editUser(userId) {
	if (confirm(`Edit user ${userId}? This would typically open an edit form.`)) {
		// In a real application, this would open a modal or form for editing
		alert(`Edit functionality for user ${userId} would be implemented here.`);
	}
}

function closeDetails() {
	document.getElementById('userDetails').style.display = 'none';
	document.querySelectorAll('.user-row').forEach(r => r.classList.remove('highlight'));
}

function resetView() {
	// Reset search
	document.getElementById('searchInput').value = '';

	// Reset sort
	const tbody = document.querySelector('#usersTable tbody');
	while (tbody.firstChild) {
		tbody.removeChild(tbody.firstChild);
	}

	// Add original rows
	allUsers.forEach(user => tbody.appendChild(user.cloneNode(true)));

	// Close details
	closeDetails();

	// Reset sort indicators
	document.querySelectorAll('th').forEach(th => {
		th.textContent = th.textContent.replace(' ↑', '').replace(' ↓', '');
	});

	currentSort = { column: null, direction: 'asc' };
}

// Add contains selector for older browsers
if (!Element.prototype.matches) {
	Element.prototype.matches = Element.prototype.msMatchesSelector;
}
