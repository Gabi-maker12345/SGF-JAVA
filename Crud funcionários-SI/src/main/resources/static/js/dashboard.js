document.addEventListener('DOMContentLoaded', () => {
    const html = document.documentElement;
    const storedTheme = localStorage.getItem('si-theme');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (storedTheme === 'dark' || (!storedTheme && prefersDark)) {
        html.classList.add('dark');
    }

    const refreshIcons = () => {
        if (window.lucide) {
            window.lucide.createIcons();
        }
    };

    const openModal = (id) => {
        const modal = document.getElementById(id);
        if (!modal) {
            return;
        }
        modal.classList.remove('closing');
        modal.classList.add('open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
        refreshIcons();
    };

    const closeModal = (modal) => {
        if (!modal) {
            return;
        }
        modal.classList.add('closing');
        window.setTimeout(() => {
            modal.classList.remove('open', 'closing');
            modal.setAttribute('aria-hidden', 'true');
            document.body.style.overflow = document.querySelector('.modal.open') ? 'hidden' : '';
        }, 150);
    };

    document.querySelectorAll('[data-open-modal]').forEach((trigger) => {
        trigger.addEventListener('click', () => openModal(trigger.dataset.openModal));
    });

    document.querySelectorAll('[data-close-modal]').forEach((trigger) => {
        trigger.addEventListener('click', () => closeModal(trigger.closest('.modal')));
    });

    document.querySelectorAll('.modal').forEach((modal) => {
        modal.addEventListener('click', (event) => {
            if (event.target === modal) {
                closeModal(modal);
            }
        });
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            document.querySelectorAll('.modal.open').forEach(closeModal);
        }
    });

    const pageMessage = document.body.dataset.pageMessage;
    if (pageMessage && pageMessage.trim() !== '') {
        const messageText = document.getElementById('messageText');
        if (messageText) {
            messageText.textContent = pageMessage;
            openModal('messageModal');
        }
    }

    const themeToggle = document.getElementById('themeToggle');
    const darkSwitch = document.getElementById('darkSwitch');

    const syncThemeSwitch = () => {
        if (darkSwitch) {
            darkSwitch.checked = html.classList.contains('dark');
        }
    };

    const toggleTheme = () => {
        html.classList.toggle('dark');
        localStorage.setItem('si-theme', html.classList.contains('dark') ? 'dark' : 'light');
        syncThemeSwitch();
        refreshIcons();
    };

    themeToggle?.addEventListener('click', toggleTheme);
    darkSwitch?.addEventListener('change', toggleTheme);
    syncThemeSwitch();

    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    const menuToggle = document.getElementById('menuToggle');

    const setMenu = (open) => {
        if (!sidebar || !overlay) {
            return;
        }
        sidebar.classList.toggle('-translate-x-[110%]', !open);
        overlay.classList.toggle('hidden', !open);
    };

    menuToggle?.addEventListener('click', () => setMenu(true));
    overlay?.addEventListener('click', () => setMenu(false));

    document.querySelectorAll('.nav-link[href^="#"]').forEach((link) => {
        link.addEventListener('click', () => {
            document.querySelectorAll('.nav-link').forEach((item) => item.classList.remove('active'));
            link.classList.add('active');
            setMenu(false);
        });
    });

    const search = document.getElementById('employeeSearch');
    search?.addEventListener('input', () => {
        const query = search.value.trim().toLowerCase();
        document.querySelectorAll('.employee-row').forEach((row) => {
            row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
        });
    });

    refreshIcons();
});
