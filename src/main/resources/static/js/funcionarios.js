/* ============================================================
   FUNCIONARIOS — Client-side CRUD
   ============================================================ */
(function () {
    'use strict';

    /* ── DOM refs ── */
    var tbody        = document.getElementById('fnTbody');
    var tableWrap    = document.getElementById('fnTableWrap');
    var emptyState   = document.getElementById('fnEmpty');
    var loading      = document.getElementById('fnLoading');
    var countEl      = document.getElementById('fnCount');
    var statTotal    = document.getElementById('statTotal');
    var statActivos  = document.getElementById('statActivos');
    var statInactivos= document.getElementById('statInactivos');

    var btnAdd       = document.getElementById('btnAdd');
    var overlay      = document.getElementById('modalOverlay');
    var modalTitle   = document.getElementById('modalTitle');
    var modalClose   = document.getElementById('modalClose');
    var btnCancel    = document.getElementById('btnCancel');
    var form         = document.getElementById('fnForm');
    var formError    = document.getElementById('formError');

    var fId          = document.getElementById('fId');
    var fNombre      = document.getElementById('fNombre');
    var fPuesto      = document.getElementById('fPuesto');
    var btnSave      = document.getElementById('btnSave');

    var toast        = document.getElementById('toast');

    var API = '/funcionarios/api';

    /* ── CSRF helper ── */
    function getCsrfToken() {
        var match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        return match ? decodeURIComponent(match[1]) : '';
    }

    function apiHeaders() {
        return {
            'Content-Type': 'application/json',
            'X-XSRF-TOKEN': getCsrfToken()
        };
    }

    /* ── Toast ── */
    var toastTimer;
    function showToast(msg, isError) {
        clearTimeout(toastTimer);
        toast.textContent = msg;
        toast.className = 'fn-toast show' + (isError ? ' fn-toast--error' : '');
        toastTimer = setTimeout(function () { toast.className = 'fn-toast'; }, 3000);
    }

    /* ── Modal ── */
    function openModal(editMode, data) {
        formError.style.display = 'none';
        fId.value = '';
        fNombre.value = '';
        fPuesto.value = '';

        if (editMode && data) {
            modalTitle.textContent = 'Editar Funcionario';
            fId.value = data.id;
            fNombre.value = data.nombre;
            fPuesto.value = data.puesto;
        } else {
            modalTitle.textContent = 'Nuevo Funcionario';
        }

        overlay.classList.add('open');
        setTimeout(function () { fNombre.focus(); }, 150);
    }

    function closeModal() {
        overlay.classList.remove('open');
    }

    btnAdd.addEventListener('click', function () { openModal(false); });
    modalClose.addEventListener('click', closeModal);
    btnCancel.addEventListener('click', closeModal);
    overlay.addEventListener('click', function (e) {
        if (e.target === overlay) closeModal();
    });

    /* ── Form submit ── */
    form.addEventListener('submit', function (e) {
        e.preventDefault();
        formError.style.display = 'none';

        var id = fId.value;
        var payload = {
            nombre: fNombre.value.trim(),
            puesto: fPuesto.value.trim()
        };

        btnSave.disabled = true;
        btnSave.textContent = 'Guardando…';

        var url = id ? API + '/' + id : API;
        var method = id ? 'PUT' : 'POST';

        fetch(url, { method: method, headers: apiHeaders(), body: JSON.stringify(payload) })
            .then(function (res) { return res.json().then(function (d) { return { ok: res.ok, data: d }; }); })
            .then(function (r) {
                if (!r.ok) {
                    formError.textContent = r.data.error || 'Error desconocido';
                    formError.style.display = '';
                    return;
                }
                closeModal();
                showToast(id ? 'Funcionario actualizado' : 'Funcionario creado');
                loadFuncionarios();
            })
            .catch(function () {
                formError.textContent = 'Error de conexión';
                formError.style.display = '';
            })
            .finally(function () {
                btnSave.disabled = false;
                btnSave.textContent = 'Guardar';
            });
    });

    /* ── Toggle activo ── */
    function toggleActivo(id, currentlyActive) {
        var action = currentlyActive ? 'desactivar' : 'reactivar';
        if (!confirm('¿Deseas ' + action + ' este funcionario?')) return;

        fetch(API + '/' + id + '/toggle', { method: 'PATCH', headers: apiHeaders() })
            .then(function (res) {
                if (!res.ok) throw new Error();
                showToast(currentlyActive ? 'Funcionario desactivado' : 'Funcionario reactivado');
                loadFuncionarios();
            })
            .catch(function () { showToast('Error al cambiar estado', true); });
    }

    /* ── Edit ── */
    function editFuncionario(id) {
        fetch(API + '/' + id, { headers: apiHeaders() })
            .then(function (res) { return res.json(); })
            .then(function (data) { openModal(true, data); })
            .catch(function () { showToast('Error al cargar funcionario', true); });
    }

    /* ── Render table ── */
    function renderFuncionarios(items) {
        if (!items.length) {
            tableWrap.style.display = 'none';
            emptyState.style.display = '';
            countEl.textContent = '0 registros';
            statTotal.textContent = '0';
            statActivos.textContent = '0';
            statInactivos.textContent = '0';
            return;
        }

        tableWrap.style.display = '';
        emptyState.style.display = 'none';
        countEl.textContent = items.length + ' registro' + (items.length !== 1 ? 's' : '');

        var activos = 0, inactivos = 0;
        items.forEach(function (f) { f.activo ? activos++ : inactivos++; });
        statTotal.textContent = items.length;
        statActivos.textContent = activos;
        statInactivos.textContent = inactivos;

        var html = '';
        items.forEach(function (f) {
            var initial = f.nombre.charAt(0).toUpperCase();
            var isActive = f.activo;
            var rowClass = isActive ? '' : ' class="row-inactive"';
            var avatarCls = isActive ? 'fn-avatar' : 'fn-avatar fn-avatar--inactive';
            var statusCls = isActive ? 'fn-status fn-status--activo' : 'fn-status fn-status--inactivo';
            var statusText = isActive ? 'Activo' : 'Inactivo';

            var toggleIcon = isActive
                ? '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/></svg>'
                : '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M21.5 2v6h-6"/><path d="M21.34 15.57a10 10 0 1 1-.57-8.38"/></svg>';
            var toggleCls = isActive ? 'fn-action-btn fn-action-btn--danger' : 'fn-action-btn fn-action-btn--restore';
            var toggleTitle = isActive ? 'Desactivar' : 'Reactivar';

            html += '<tr' + rowClass + '>'
                + '<td><div class="fn-cell-name"><div class="' + avatarCls + '">' + initial + '</div><span class="fn-name-text">' + escHtml(f.nombre) + '</span></div></td>'
                + '<td><span class="fn-puesto">' + escHtml(f.puesto) + '</span></td>'
                + '<td><span class="' + statusCls + '"><span class="fn-status-dot"></span>' + statusText + '</span></td>'
                + '<td class="td-actions">'
                    + '<button class="fn-action-btn" title="Editar" data-edit="' + f.id + '">'
                        + '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/><path d="m15 5 4 4"/></svg>'
                    + '</button>'
                    + '<button class="' + toggleCls + '" title="' + toggleTitle + '" data-toggle="' + f.id + '" data-active="' + isActive + '">'
                        + toggleIcon
                    + '</button>'
                + '</td>'
                + '</tr>';
        });

        tbody.innerHTML = html;
    }

    function escHtml(s) {
        var d = document.createElement('div');
        d.textContent = s;
        return d.innerHTML;
    }

    /* ── Delegation for table buttons ── */
    tbody.addEventListener('click', function (e) {
        var btn = e.target.closest('[data-edit]');
        if (btn) { editFuncionario(btn.getAttribute('data-edit')); return; }

        btn = e.target.closest('[data-toggle]');
        if (btn) { toggleActivo(btn.getAttribute('data-toggle'), btn.getAttribute('data-active') === 'true'); }
    });

    /* ── Load funcionarios ── */
    function loadFuncionarios() {
        loading.style.display = '';
        tableWrap.style.display = 'none';
        emptyState.style.display = 'none';

        fetch(API)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                loading.style.display = 'none';
                renderFuncionarios(data);
            })
            .catch(function () {
                loading.style.display = 'none';
                showToast('Error al cargar funcionarios', true);
            });
    }

    /* ── Init ── */
    loadFuncionarios();

})();
