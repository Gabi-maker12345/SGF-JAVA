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

    /* ─── Histórico: accordion (só um aberto de cada vez) ─── */
function toggleChangelogItem(header) {
    const clickedItem   = header.closest('.changelog-item');
    const clickedDetail = clickedItem.querySelector('.changelog-details');
    const clickedIcon   = header.querySelector('.changelog-toggle');
    const isOpen        = clickedDetail.style.display === 'block';

    // fecha todos
    document.querySelectorAll('.changelog-item').forEach(item => {
        item.querySelector('.changelog-details').style.display = 'none';
        const icon = item.querySelector('.changelog-toggle');
        if (icon) icon.style.transform = 'rotate(0deg)';
        item.classList.remove('is-open');
    });

    // abre só o clicado (se estava fechado)
    if (!isOpen) {
        clickedDetail.style.display = 'block';
        if (clickedIcon) clickedIcon.style.transform = 'rotate(180deg)';
        clickedItem.classList.add('is-open');
    }
}

/* ─── Histórico: filtro por período ─── */
function filterChangelogs(value) {
    const container    = document.getElementById('changelogContainer');
    const emptyFilter  = document.getElementById('historyFilterEmpty');
    if (!container) return;

    const items  = container.querySelectorAll('.changelog-item');
    const cutoff = value === 'all'
        ? null
        : new Date(Date.now() - Number(value) * 24 * 60 * 60 * 1000);

    let visible = 0;
    items.forEach(item => {
        const raw  = item.getAttribute('data-changed-at'); // "2025-06-01T14:30:00"
        const date = raw ? new Date(raw) : null;
        const show = !cutoff || (date && date >= cutoff);
        item.style.display = show ? '' : 'none';
        if (show) visible++;
    });

    // fecha qualquer item aberto que ficou oculto
    document.querySelectorAll('.changelog-item[style*="display: none"] .changelog-details')
        .forEach(d => { d.style.display = 'none'; });

    emptyFilter && (emptyFilter.style.display = visible === 0 ? 'block' : 'none');
}
});
