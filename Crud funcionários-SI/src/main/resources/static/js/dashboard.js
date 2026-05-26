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
        const pageMessageType = document.body.dataset.pageMessageType || 'success';
        const messageModal = document.getElementById('messageModal');
        const messageSymbol = messageModal?.querySelector('.modal-symbol');
        const messageTitle = messageModal?.querySelector('.modal-title');
        const messageText = document.getElementById('messageText');

        const modalState = {
            success: { symbol: 'success', icon: 'check', title: 'Tudo certo' },
            error: { symbol: 'danger', icon: 'triangle-alert', title: 'Validacao necessaria' },
            warning: { symbol: 'warning', icon: 'circle-alert', title: 'Atencao' },
            info: { symbol: 'info', icon: 'info', title: 'Atencao' }
        }[pageMessageType] || { symbol: 'info', icon: 'info', title: 'Atencao' };

        if (messageSymbol) {
            messageSymbol.className = `modal-symbol ${modalState.symbol}`;
            messageSymbol.innerHTML = `<i data-lucide="${modalState.icon}"></i>`;
        }

        if (messageTitle) {
            messageTitle.textContent = modalState.title;
        }

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

    const search = document.getElementById('employeeSearch');
    const sectionButtons = document.querySelectorAll('[data-section-target]');
    const viewSections = document.querySelectorAll('[data-view-section]');
    const sectionTitle = document.getElementById('sectionTitle');
    const sectionSubtitle = document.getElementById('sectionSubtitle');

    const filterActiveRows = () => {
        const activeSection = document.querySelector('[data-view-section].is-active');
        const query = search?.value.trim().toLowerCase() || '';

        document.querySelectorAll('.data-table-row').forEach((row) => {
            row.style.display = '';
        });

        if (!activeSection || query === '') {
            return;
        }

        activeSection.querySelectorAll('.data-table-row').forEach((row) => {
            row.style.display = row.textContent.toLowerCase().includes(query) ? '' : 'none';
        });
    };

    const showSection = (sectionId, updateHash = true) => {
        const target = document.getElementById(sectionId) || document.getElementById('overview');
        const activeButton = document.querySelector(`[data-section-target="${target.id}"]`);

        viewSections.forEach((section) => {
            const isActive = section === target;
            section.classList.toggle('is-active', isActive);
            section.hidden = !isActive;
        });

        sectionButtons.forEach((button) => {
            button.classList.toggle('active', button === activeButton);
        });

        if (activeButton) {
            if (sectionTitle) {
                sectionTitle.textContent = activeButton.dataset.title || activeButton.textContent.trim();
            }
            if (sectionSubtitle) {
                sectionSubtitle.textContent = activeButton.dataset.subtitle || '';
            }
        }

        if (search) {
            search.value = '';
            search.placeholder = target.id === 'departments' ? 'Pesquisar departamento' : target.id === 'employees' ? 'Pesquisar funcionario' : 'Pesquisar no painel';
        }

        if (updateHash) {
            history.replaceState(null, '', `#${target.id}`);
        }

        filterActiveRows();
        setMenu(false);
        refreshIcons();
    };

    sectionButtons.forEach((button) => {
        button.addEventListener('click', () => showSection(button.dataset.sectionTarget));
    });

    const initialSection = window.location.hash.replace('#', '') || 'overview';
    showSection(initialSection, false);

    search?.addEventListener('input', () => {
        filterActiveRows();
    });

    const fitMonetaryText = () => {
        document.querySelectorAll('.money-value, .salary-cell').forEach((element) => {
            const digits = element.textContent.replace(/\D/g, '').length;
            let size = '';

            if (element.classList.contains('money-value')) {
                const compact = Boolean(element.closest('.sidebar-insight'));
                if (digits >= 14) {
                    size = compact ? '.92rem' : '1.02rem';
                } else if (digits >= 11) {
                    size = compact ? '1.02rem' : '1.18rem';
                } else if (digits >= 8) {
                    size = compact ? '1.12rem' : '1.45rem';
                }
            } else if (digits >= 14) {
                size = '.72rem';
            } else if (digits >= 11) {
                size = '.78rem';
            } else if (digits >= 8) {
                size = '.84rem';
            }

            if (size) {
                element.style.setProperty('--fit-font-size', size);
            } else {
                element.style.removeProperty('--fit-font-size');
            }
        });
    };

    fitMonetaryText();
    refreshIcons();
});
