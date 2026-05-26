document.addEventListener('DOMContentLoaded', () => {
    const html = document.documentElement;
    const storedTheme = localStorage.getItem('si-theme');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

    if (storedTheme === 'dark' || (!storedTheme && prefersDark)) {
        html.classList.add('dark');
    }

    const fallbackIcons = {
        activity: '<polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>',
        'arrow-right': '<path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>',
        bell: '<path d="M10.3 21a2 2 0 0 0 3.4 0"/><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/>',
        check: '<path d="M20 6 9 17l-5-5"/>',
        'chart-no-axes-column': '<path d="M5 21V10"/><path d="M12 21V3"/><path d="M19 21v-6"/>',
        'circle-alert': '<circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/>',
        'circle-check': '<circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/>',
        'circle-x': '<circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/>',
        'folder-plus': '<path d="M12 10v6"/><path d="M9 13h6"/><path d="M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.7-.9L9.6 4A2 2 0 0 0 7.9 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z"/>',
        info: '<circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/>',
        'layers-3': '<path d="m12 2 9 5-9 5-9-5 9-5Z"/><path d="m3 12 9 5 9-5"/><path d="m3 17 9 5 9-5"/>',
        'layout-dashboard': '<rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/>',
        'log-out': '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/>',
        menu: '<path d="M4 6h16"/><path d="M4 12h16"/><path d="M4 18h16"/>',
        moon: '<path d="M12 3a6 6 0 0 0 9 7.4A9 9 0 1 1 12 3Z"/>',
        network: '<rect x="16" y="16" width="6" height="6" rx="1"/><rect x="2" y="16" width="6" height="6" rx="1"/><rect x="9" y="2" width="6" height="6" rx="1"/><path d="M12 8v4"/><path d="M5 16v-2a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2v2"/>',
        pencil: '<path d="M21.2 6.8a2.8 2.8 0 0 0-4-4L4 16v4h4Z"/><path d="m14 5 5 5"/>',
        'rotate-ccw': '<path d="M3 12a9 9 0 1 0 9-9 9.8 9.8 0 0 0-6.7 2.7L3 8"/><path d="M3 3v5h5"/>',
        search: '<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
        'shield-check': '<path d="M20 13c0 5-3.5 7.5-8 9-4.5-1.5-8-4-8-9V5l8-3 8 3Z"/><path d="m9 12 2 2 4-4"/>',
        sparkle: '<path d="M9.9 2.8 8.7 8.4 3.1 9.6l5.6 1.2 1.2 5.6 1.2-5.6 5.6-1.2-5.6-1.2Z"/><path d="M18 14.5 17.4 17l-2.4.5 2.4.5.6 2.5.5-2.5 2.5-.5-2.5-.5Z"/>',
        sparkles: '<path d="m12 3-1.9 5.1L5 10l5.1 1.9L12 17l1.9-5.1L19 10l-5.1-1.9Z"/><path d="M5 3v4"/><path d="M3 5h4"/><path d="M19 17v4"/><path d="M17 19h4"/>',
        'trending-up': '<path d="m22 7-8.5 8.5-5-5L2 17"/><path d="M16 7h6v6"/>',
        'trash-2': '<path d="M3 6h18"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/>',
        'triangle-alert': '<path d="m21.7 18-8-14a2 2 0 0 0-3.4 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.7-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/>',
        'user-plus': '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M19 8v6"/><path d="M22 11h-6"/>',
        'user-round-plus': '<path d="M2 21a8 8 0 0 1 13.3-6"/><circle cx="10" cy="8" r="5"/><path d="M19 16v6"/><path d="M22 19h-6"/>',
        'users-round': '<path d="M18 21a8 8 0 0 0-16 0"/><circle cx="10" cy="8" r="5"/><path d="M22 20c0-3-1.6-5.5-4-6.7"/><path d="M16 3.1a5 5 0 0 1 0 9.8"/>',
        wallet: '<path d="M19 7V5a2 2 0 0 0-2-2H5a3 3 0 0 0 0 6h14a2 2 0 0 1 2 2v4a2 2 0 0 1-2 2H5a3 3 0 0 1-3-3V6"/><path d="M16 14h.01"/>',
        x: '<path d="M18 6 6 18"/><path d="m6 6 12 12"/>'
    };

    const createFallbackIcons = () => {
        document.querySelectorAll('i[data-lucide]').forEach((icon) => {
            const name = icon.dataset.lucide;
            const paths = fallbackIcons[name] || fallbackIcons.info;
            const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
            svg.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
            svg.setAttribute('viewBox', '0 0 24 24');
            svg.setAttribute('fill', 'none');
            svg.setAttribute('stroke', 'currentColor');
            svg.setAttribute('stroke-width', '2');
            svg.setAttribute('stroke-linecap', 'round');
            svg.setAttribute('stroke-linejoin', 'round');
            svg.setAttribute('class', 'lucide lucide-fallback');
            svg.setAttribute('aria-hidden', 'true');
            svg.innerHTML = paths;
            icon.replaceWith(svg);
        });
    };

    const refreshIcons = () => {
        if (window.lucide) {
            window.lucide.createIcons();
        }
        createFallbackIcons();
    };

    refreshIcons();

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

    const clearLoginForm = () => {
        const loginForm = document.getElementById('loginForm');
        if (!loginForm) {
            return;
        }
        loginForm.reset();
        loginForm.querySelectorAll('input').forEach((input) => {
            input.value = '';
            input.setAttribute('autocomplete', input.type === 'password' ? 'new-password' : 'off');
        });
    };

    clearLoginForm();
    window.addEventListener('pageshow', clearLoginForm);

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

    const PHONE_PATTERN = /^\+244\d{9}$/;
    const MINIMUM_SALARY = 100000;
    const PHONE_MESSAGE = 'O telefone deve seguir o formato angolano +244000000000';
    const SALARY_MESSAGE = 'O salário mínimo angolano é 100.000kzs, insira valores apartir disso';

    const getFormErrorBox = (form) => {
        let errorBox = form.querySelector('.modal-inline-error');
        if (!errorBox) {
            errorBox = document.createElement('div');
            errorBox.className = 'modal-inline-error';
            errorBox.setAttribute('role', 'alert');
            errorBox.setAttribute('aria-live', 'polite');
            form.prepend(errorBox);
        }
        return errorBox;
    };

    const clearFormError = (form) => {
        const errorBox = form.querySelector('.modal-inline-error');
        if (errorBox) {
            errorBox.hidden = true;
            errorBox.textContent = '';
        }

        form.querySelectorAll('.field-input.is-invalid').forEach((field) => {
            field.classList.remove('is-invalid');
            field.removeAttribute('aria-invalid');
        });
    };

    const showFormError = (form, message, field) => {
        const errorBox = getFormErrorBox(form);
        errorBox.textContent = message;
        errorBox.hidden = false;

        if (field) {
            field.classList.add('is-invalid');
            field.setAttribute('aria-invalid', 'true');
            field.focus({ preventScroll: true });
        }
    };

    const validateEmployeeForm = (form) => {
        const phone = form.querySelector('[name="phone"]');
        const salary = form.querySelector('[name="salary"]');
        const phoneValue = phone?.value.trim() || '';
        const salaryValue = Number(salary?.value || 0);

        clearFormError(form);

        if (phoneValue !== '' && !PHONE_PATTERN.test(phoneValue)) {
            showFormError(form, PHONE_MESSAGE, phone);
            return false;
        }

        if (!Number.isFinite(salaryValue) || salaryValue < MINIMUM_SALARY) {
            showFormError(form, SALARY_MESSAGE, salary);
            return false;
        }

        return true;
    };

    const parsePageMessage = async (response) => {
        const htmlText = await response.text();
        const parsedPage = new DOMParser().parseFromString(htmlText, 'text/html');
        return {
            message: parsedPage.body?.dataset.pageMessage || '',
            messageType: parsedPage.body?.dataset.pageMessageType || ''
        };
    };

    document.querySelectorAll('#employeeModal form, [id^="editEmployeeModal-"] form').forEach((form) => {
        form.dataset.keepErrorsInModal = 'true';
        form.noValidate = true;
    });

    document.querySelectorAll('form[data-keep-errors-in-modal="true"]').forEach((form) => {
        form.addEventListener('input', () => clearFormError(form));
        form.addEventListener('submit', async (event) => {
            event.preventDefault();

            if (!validateEmployeeForm(form)) {
                return;
            }

            const submitButton = form.querySelector('[type="submit"]');
            submitButton?.setAttribute('disabled', 'disabled');

            try {
                const response = await fetch(form.action, {
                    method: form.method || 'POST',
                    body: new FormData(form),
                    credentials: 'same-origin',
                    redirect: 'follow'
                });
                const { message, messageType } = await parsePageMessage(response);

                if (messageType === 'error') {
                    showFormError(form, message || 'Nao foi possivel concluir a accao. Verifique os dados e tente novamente.');
                    return;
                }

                window.location.href = response.url || form.action;
            } catch (error) {
                showFormError(form, 'Nao foi possivel comunicar com o servidor. Tente novamente.');
            } finally {
                submitButton?.removeAttribute('disabled');
            }
        });
    });

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
        if (!target || viewSections.length === 0) {
            return;
        }

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

    if (viewSections.length > 0) {
        sectionButtons.forEach((button) => {
            button.addEventListener('click', () => showSection(button.dataset.sectionTarget));
        });

        const initialSection = window.location.hash.replace('#', '') || 'overview';
        showSection(initialSection, false);

        search?.addEventListener('input', () => {
            filterActiveRows();
        });
    }

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
