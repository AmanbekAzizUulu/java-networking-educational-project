// Add interactive functionality
document.addEventListener('DOMContentLoaded', function () {
	// Add click handlers for examples
	const examples = document.querySelectorAll('.example');
	examples.forEach(example => {
		example.addEventListener('click', function () {
			this.classList.toggle('expanded');
		});
	});

	// Add smooth scrolling for method cards
	const cards = document.querySelectorAll('.method-card');
	cards.forEach(card => {
		card.addEventListener('click', function (e) {
			if (e.target.tagName !== 'A') {
				this.scrollIntoView({ behavior: 'smooth', block: 'center' });
			}
		});
	});

	// Add search functionality
	addSearchFunctionality();

	// Add filter functionality
	addFilterFunctionality();
});

// Search functionality
function addSearchFunctionality() {
	const header = document.querySelector('.header');
	const searchHTML = `
        <div class="search-container" style="margin-top: 20px;">
            <input type="text" id="searchInput" placeholder="Search HTTP methods..."
                   style="padding: 12px 20px; width: 300px; max-width: 100%;
                          border: none; border-radius: 25px; font-size: 16px;
                          box-shadow: 0 4px 15px rgba(0,0,0,0.2);" />
        </div>
    `;
	header.insertAdjacentHTML('beforeend', searchHTML);

	const searchInput = document.getElementById('searchInput');
	searchInput.addEventListener('input', function () {
		const searchTerm = this.value.toLowerCase();
		const cards = document.querySelectorAll('.method-card');

		cards.forEach(card => {
			const methodName = card.querySelector('.method-name').textContent.toLowerCase();
			const methodDescription = card.querySelector('.method-description').textContent.toLowerCase();
			const useCases = Array.from(card.querySelectorAll('.use-cases li')).map(li => li.textContent.toLowerCase());

			const matches = methodName.includes(searchTerm) ||
				methodDescription.includes(searchTerm) ||
				useCases.some(useCase => useCase.includes(searchTerm));

			card.style.display = matches ? 'block' : 'none';
		});
	});
}

// Filter functionality
function addFilterFunctionality() {
	const header = document.querySelector('.header');
	const filterHTML = `
        <div class="filter-container" style="margin-top: 15px;">
            <select id="methodFilter" style="padding: 10px 15px; border: none; border-radius: 20px;
                    font-size: 14px; margin-right: 10px; box-shadow: 0 4px 15px rgba(0,0,0,0.2);">
                <option value="all">All Methods</option>
                <option value="GET">GET</option>
                <option value="POST">POST</option>
                <option value="PUT">PUT</option>
                <option value="DELETE">DELETE</option>
                <option value="PATCH">PATCH</option>
            </select>
            <button id="resetFilters" style="padding: 10px 20px; border: none; border-radius: 20px;
                    background: white; color: #333; cursor: pointer; font-size: 14px;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.2);">Reset</button>
        </div>
    `;
	header.insertAdjacentHTML('beforeend', filterHTML);

	const methodFilter = document.getElementById('methodFilter');
	const resetButton = document.getElementById('resetFilters');

	methodFilter.addEventListener('change', function () {
		const selectedMethod = this.value;
		const cards = document.querySelectorAll('.method-card');

		cards.forEach(card => {
			const methodName = card.querySelector('.method-name').textContent;
			if (selectedMethod === 'all' || methodName.includes(selectedMethod)) {
				card.style.display = 'block';
			} else {
				card.style.display = 'none';
			}
		});
	});

	resetButton.addEventListener('click', function () {
		methodFilter.value = 'all';
		const cards = document.querySelectorAll('.method-card');
		cards.forEach(card => card.style.display = 'block');

		const searchInput = document.getElementById('searchInput');
		if (searchInput) searchInput.value = '';
	});
}

// Add keyboard shortcuts
document.addEventListener('keydown', function (e) {
	// Ctrl+F for search
	if (e.ctrlKey && e.key === 'f') {
		e.preventDefault();
		const searchInput = document.getElementById('searchInput');
		if (searchInput) searchInput.focus();
	}

	// Escape to reset
	if (e.key === 'Escape') {
		const resetButton = document.getElementById('resetFilters');
		if (resetButton) resetButton.click();
	}
});
