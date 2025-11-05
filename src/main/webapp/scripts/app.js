// Initialize application when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
	initializeApp();
	enhanceWithNerdFonts();
});

function initializeApp() {
	setCopyrightYear();
	setupContactForm();
	setupSmoothScrolling();
	setupFormValidation();
}

// Enhance UI with Nerd Font icons
function enhanceWithNerdFonts() {
	// Add Nerd Font icons to specific elements if needed
	const brandElement = document.querySelector('.brand');
	if (brandElement) {
		// You can add icons to brand if desired
		// brandElement.innerHTML = '&#xf489; ' + brandElement.innerHTML;
	}

	// Add icons to navigation items
	const navItems = document.querySelectorAll('nav a');
	navItems.forEach(item => {
		const text = item.textContent;
		if (text.includes('Services')) {
			// item.innerHTML = '&#xf0c2; ' + item.innerHTML; // nf-oct-gear
		} else if (text.includes('About')) {
			// item.innerHTML = '&#xf468; ' + item.innerHTML; // nf-oct-person
		} else if (text.includes('Contact')) {
			// item.innerHTML = '&#xf0e0; ' + item.innerHTML; // nf-oct-mail
		}
	});

	// Add icons to service cards
	const serviceCards = document.querySelectorAll('.card strong');
	serviceCards.forEach(card => {
		const serviceName = card.textContent;
		if (serviceName.includes('Web & Backend')) {
			// card.innerHTML = '&#xf308; ' + card.innerHTML; // nf-oct-browser
		} else if (serviceName.includes('Mobile & Frontend')) {
			// card.innerHTML = '&#xf3cd; ' + card.innerHTML; // nf-oct-mobile
		} else if (serviceName.includes('Cloud & DevOps')) {
			// card.innerHTML = '&#xf0c2; ' + card.innerHTML; // nf-oct-gear
		} else if (serviceName.includes('Consulting & QA')) {
			// card.innerHTML = '&#xf468; ' + card.innerHTML; // nf-oct-person
		}
	});
}

// Set current year in footer
function setCopyrightYear() {
	const yearElement = document.getElementById('year');
	if (yearElement) {
		yearElement.textContent = new Date().getFullYear();
	}
}

// Setup contact form handling
function setupContactForm() {
	const contactForm = document.getElementById('contact');
	if (contactForm) {
		contactForm.addEventListener('submit', handleFormSubmit);
	}
}

// Handle form submission with validation
function handleFormSubmit(e) {
	e.preventDefault();

	if (validateForm()) {
		// Form is valid, you can proceed with submission
		submitForm();
	}
}

// Validate form fields
function validateForm() {
	const email = document.querySelector('input[type="email"]').value.trim();
	const message = document.querySelector('textarea').value.trim();
	const name = document.querySelector('input[name="name"]').value.trim();

	let isValid = true;
	let errorMessage = '';

	// Clear previous error styles
	clearErrorStyles();

	// Validate name
	if (!name) {
		markFieldError('input[name="name"]');
		errorMessage += '• Please provide your name\n';
		isValid = false;
	}

	// Validate email
	if (!email) {
		markFieldError('input[type="email"]');
		errorMessage += '• Please provide an email address\n';
		isValid = false;
	} else if (!isValidEmail(email)) {
		markFieldError('input[type="email"]');
		errorMessage += '• Please provide a valid email address\n';
		isValid = false;
	}

	// Validate message
	if (!message) {
		markFieldError('textarea');
		errorMessage += '• Please provide a project description\n';
		isValid = false;
	} else if (message.length < 10) {
		markFieldError('textarea');
		errorMessage += '• Please provide a more detailed project description\n';
		isValid = false;
	}

	if (!isValid) {
		showError(errorMessage);
	}

	return isValid;
}

// Check if email is valid
function isValidEmail(email) {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
	return emailRegex.test(email);
}

// Mark field with error style
function markFieldError(selector) {
	const field = document.querySelector(selector);
	if (field) {
		field.style.borderColor = '#dc2626';
		field.style.backgroundColor = '#fef2f2';
	}
}

// Clear all error styles
function clearErrorStyles() {
	const fields = document.querySelectorAll('input, textarea');
	fields.forEach(field => {
		field.style.borderColor = '';
		field.style.backgroundColor = '';
	});
}

// Show error message
function showError(message) {
	// Remove existing error message
	const existingError = document.querySelector('.error-message');
	if (existingError) {
		existingError.remove();
	}

	// Create new error message
	const errorDiv = document.createElement('div');
	errorDiv.className = 'error-message';
	errorDiv.style.cssText = `
        background: #fef2f2;
        border: 1px solid #fecaca;
        color: #dc2626;
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 16px;
        white-space: pre-line;
        font-size: 0.9rem;
    `;
	errorDiv.textContent = message;

	const form = document.getElementById('contact');
	form.insertBefore(errorDiv, form.firstChild);

	// Scroll to error message
	errorDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// Submit form (mock implementation)
function submitForm() {
	const form = document.getElementById('contact');
	const formData = new FormData(form);

	// Show loading state
	const submitButton = form.querySelector('button[type="submit"]');
	const originalText = submitButton.textContent;
	submitButton.textContent = 'Sending...';
	submitButton.disabled = true;

	// Simulate API call
	setTimeout(() => {
		// In real application, you would send data to server
		console.log('Form submitted with data:', Object.fromEntries(formData));

		// Show success message
		showSuccessMessage();

		// Reset form
		form.reset();

		// Restore button
		submitButton.textContent = originalText;
		submitButton.disabled = false;
	}, 1500);
}

// Show success message
function showSuccessMessage() {
	// Remove existing messages
	const existingMessage = document.querySelector('.success-message, .error-message');
	if (existingMessage) {
		existingMessage.remove();
	}

	const successDiv = document.createElement('div');
	successDiv.className = 'success-message';
	successDiv.style.cssText = `
        background: #f0fdf4;
        border: 1px solid #bbf7d0;
        color: #16a34a;
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 16px;
        font-size: 0.9rem;
    `;
	successDiv.textContent = 'Thank you! Your message has been sent. We\'ll get back to you soon.';

	const form = document.getElementById('contact');
	form.insertBefore(successDiv, form.firstChild);

	// Scroll to success message
	successDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// Setup smooth scrolling for anchor links
function setupSmoothScrolling() {
	const anchorLinks = document.querySelectorAll('a[href^="#"]');

	anchorLinks.forEach(link => {
		link.addEventListener('click', function (e) {
			const href = this.getAttribute('href');

			if (href !== '#') {
				e.preventDefault();
				const target = document.querySelector(href);

				if (target) {
					const headerHeight = document.querySelector('header').offsetHeight;
					const targetPosition = target.offsetTop - headerHeight - 20;

					window.scrollTo({
						top: targetPosition,
						behavior: 'smooth'
					});
				}
			}
		});
	});
}

// Setup real-time form validation
function setupFormValidation() {
	const formFields = document.querySelectorAll('#contact input, #contact textarea');

	formFields.forEach(field => {
		field.addEventListener('input', function () {
			// Clear error styles when user starts typing
			this.style.borderColor = '';
			this.style.backgroundColor = '';

			// Remove error message if all fields are valid
			const errorMessage = document.querySelector('.error-message');
			if (errorMessage) {
				const email = document.querySelector('input[type="email"]').value.trim();
				const message = document.querySelector('textarea').value.trim();

				if (email && message && isValidEmail(email)) {
					errorMessage.remove();
				}
			}
		});
	});
}
