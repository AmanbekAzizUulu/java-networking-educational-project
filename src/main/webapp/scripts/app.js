document.addEventListener('DOMContentLoaded', function() {
    // Initialize the application
    initApp();
});

function initApp() {
    initializeNavigation();
    initializeRoleSelection();
    initializeModals();
    initializeAnimations();
    initializeSmoothScroll();
}

// Navigation functionality
function initializeNavigation() {
    const hamburger = document.querySelector('.hamburger');
    const navMenu = document.querySelector('.nav-menu');
    const navActions = document.querySelector('.nav-actions');

    if (hamburger) {
        hamburger.addEventListener('click', function() {
            this.classList.toggle('active');
            navMenu.classList.toggle('active');
            navActions.classList.toggle('active');
        });
    }

    // Navigation buttons
    const navLoginBtn = document.getElementById('navLoginBtn');
    const navRegisterBtn = document.getElementById('navRegisterBtn');

    if (navLoginBtn) {
        navLoginBtn.addEventListener('click', function() {
            showRoleSelectionModal('login');
        });
    }

    if (navRegisterBtn) {
        navRegisterBtn.addEventListener('click', function() {
            showRoleSelectionModal('register');
        });
    }
}

// Role selection functionality
function initializeRoleSelection() {
    const roleCards = document.querySelectorAll('.role-card');
    const roleLoginButtons = document.querySelectorAll('.role-login');
    const roleRegisterButtons = document.querySelectorAll('.role-register');

    // Role card selection
    roleCards.forEach(card => {
        card.addEventListener('click', function() {
            // Remove selected class from all cards
            roleCards.forEach(c => c.classList.remove('selected'));
            // Add selected class to clicked card
            this.classList.add('selected');
        });
    });

    // Role-specific login buttons
    roleLoginButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const role = this.getAttribute('data-role');
            redirectToLogin(role);
        });
    });

    // Role-specific register buttons
    roleRegisterButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.stopPropagation();
            const role = this.getAttribute('data-role');
            redirectToRegister(role);
        });
    });

    // Hero section buttons
    const heroRegisterBtn = document.getElementById('heroRegisterBtn');
    const heroDemoBtn = document.getElementById('heroDemoBtn');

    if (heroRegisterBtn) {
        heroRegisterBtn.addEventListener('click', function() {
            showRoleSelectionModal('register');
        });
    }

    if (heroDemoBtn) {
        heroDemoBtn.addEventListener('click', function() {
            showDemoModal();
        });
    }

    // CTA buttons
    const ctaRegisterBtn = document.getElementById('ctaRegisterBtn');
    const ctaContactBtn = document.getElementById('ctaContactBtn');

    if (ctaRegisterBtn) {
        ctaRegisterBtn.addEventListener('click', function() {
            showRoleSelectionModal('register');
        });
    }

    if (ctaContactBtn) {
        ctaContactBtn.addEventListener('click', function() {
            showDemoModal();
        });
    }
}

// Modal functionality
function initializeModals() {
    const demoModal = document.getElementById('demoModal');
    const closeButtons = document.querySelectorAll('.close');

    // Close modal when clicking X
    closeButtons.forEach(button => {
        button.addEventListener('click', function() {
            demoModal.style.display = 'none';
        });
    });

    // Close modal when clicking outside
    window.addEventListener('click', function(event) {
        if (event.target === demoModal) {
            demoModal.style.display = 'none';
        }
    });

    // Demo form submission
    const demoForm = document.querySelector('.demo-form');
    if (demoForm) {
        demoForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const formData = new FormData(this);
            // In a real app, you would send this data to your server
            alert('Спасибо! Наш специалист свяжется с вами в ближайшее время.');
            demoModal.style.display = 'none';
            this.reset();
        });
    }
}

// Animation functionality
function initializeAnimations() {
    // Add scroll animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
            }
        });
    }, observerOptions);

    // Observe elements for animation
    const animatedElements = document.querySelectorAll('.feature-card, .role-card, .stat-item');
    animatedElements.forEach(el => {
        observer.observe(el);
    });
}

// Smooth scroll functionality
function initializeSmoothScroll() {
    const navLinks = document.querySelectorAll('a[href^="#"]');

    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();

            const targetId = this.getAttribute('href');
            if (targetId === '#') return;

            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                const offsetTop = targetElement.offsetTop - 80;

                window.scrollTo({
                    top: offsetTop,
                    behavior: 'smooth'
                });
            }
        });
    });
}

// Show role selection modal
function showRoleSelectionModal(action) {
    // In a real app, this would show a modal for role selection
    // For now, we'll use the existing role cards section
    const roleSection = document.querySelector('.role-section');
    if (roleSection) {
        roleSection.scrollIntoView({ behavior: 'smooth' });

        // Highlight the action
        setTimeout(() => {
            alert(`Пожалуйста, выберите вашу роль для ${action === 'login' ? 'входа' : 'регистрации'}`);
        }, 500);
    }
}

// Show demo modal
function showDemoModal() {
    const demoModal = document.getElementById('demoModal');
    if (demoModal) {
        demoModal.style.display = 'block';
    }
}

// Redirect to login page
function redirectToLogin(role) {
    // In a real app, this would redirect to the actual login page
    // For demo purposes, we'll show an alert
    const roleNames = {
        'patient': 'пациента',
        'doctor': 'врача',
        'admin': 'администратора'
    };

    alert(`Переход на страницу входа для ${roleNames[role]}`);
    console.log(`Redirecting to login page for: ${role}`);

    // Actual redirect would be:
    // window.location.href = `login.html?role=${role}`;
}

// Redirect to register page
function redirectToRegister(role) {
    // In a real app, this would redirect to the actual register page
    // For demo purposes, we'll show an alert
    const roleNames = {
        'patient': 'пациента',
        'doctor': 'врача',
        'admin': 'администратора'
    };

    alert(`Переход на страницу регистрации для ${roleNames[role]}`);
    console.log(`Redirecting to register page for: ${role}`);

    // Actual redirect would be:
    // window.location.href = `register.html?role=${role}`;
}

// Add some CSS for animations
const style = document.createElement('style');
style.textContent = `
    .animate-in {
        animation: fadeInUp 0.6s ease forwards;
    }

    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(30px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .feature-card,
    .role-card,
    .stat-item {
        opacity: 0;
    }

    .nav-menu.active,
    .nav-actions.active {
        display: flex !important;
        flex-direction: column;
        position: absolute;
        top: 100%;
        left: 0;
        width: 100%;
        background: white;
        box-shadow: var(--shadow);
        padding: 1rem;
    }

    .hamburger.active span:nth-child(1) {
        transform: rotate(45deg) translate(5px, 5px);
    }

    .hamburger.active span:nth-child(2) {
        opacity: 0;
    }

    .hamburger.active span:nth-child(3) {
        transform: rotate(-45deg) translate(7px, -6px);
    }

    @media (max-width: 768px) {
        .nav-menu,
        .nav-actions {
            display: none;
        }
    }
`;
document.head.appendChild(style);
