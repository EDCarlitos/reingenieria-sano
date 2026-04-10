/* ============================================================
   USUARIOS — Client-side CRUD
   ============================================================ */
(function () {
    'use strict';

    /* ── DOM refs ── */
    var tbody        = document.getElementById('usrTbody');
    var tableWrap    = document.getElementById('usrTableWrap');
    var emptyState   = document.getElementById('usrEmpty');
    var loading      = document.getElementById('usrLoading');
    var countEl      = document.getElementById('usrCount');
    var statTotal    = document.getElementById('statTotal');
    var statActivos  = document.getElementById('statActivos');
    var statInactivos= document.getElementById('statInactivos');

    var btnAdd       = document.getElementById('btnAdd');
    var overlay      = document.getElementById('modalOverlay');
    var modalTitle   = document.getElementById('modalTitle');
    var modalClose   = document.getElementById('modalClose');
    var btnCancel    = document.getElementById('btnCancel');
    var form         = document.getElementById('usrForm');
    var formError    = document.getElementById('formError');
    var pwdHint      = document.getElementById('pwdHint');

    var fId          = document.getElementById('fId');
    var fUsername     = document.getElementById('fUsername');
    var fPassword    = document.getElementById('fPassword');
    var fRol         = document.getElementById('fRol');
    var btnSave      = document.getElementById('btnSave');

    var toast        = document.getElementById('toast');

    var API = '/usuarios/api';

    /* ── CSRF helper — read from XSRF-TOKEN cookie ── */
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
        toast.className = 'usr-toast show' + (isError ? ' usr-toast--error' : '');
        toastTimer = setTimeout(function () { toast.className = 'usr-toast'; }, 3000);
    }

    /* ── Modal ── */
    function openModal(editMode, data) {
        formError.style.display = 'none';
        fId.value = '';
        fUsername.value = '';
        fPassword.value = '';
        fRol.value = 'EMPLEADO';

        if (editMode && data) {
            modalTitle.textContent = 'Editar Usuario';
            fId.value = data.id;
            fUsername.value = data.username;
            fRol.value = data.rol;
            pwdHint.style.display = '';
            fPassword.removeAttribute('required');
        } else {
            modalTitle.textContent = 'Nuevo Usuario';
            pwdHint.style.display = 'none';
            fPassword.setAttribute('required', '');
        }

        overlay.classList.add('open');
        setTimeout(function () { fUsername.focus(); }, 150);
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
            username: fUsername.value.trim(),
            password: fPassword.value,
            rol: fRol.value
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
                showToast(id ? 'Usuario actualizado' : 'Usuario creado');
                loadUsers();
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
        if (!confirm('¿Deseas ' + action + ' este usuario?')) return;

        fetch(API + '/' + id + '/toggle', { method: 'PATCH', headers: apiHeaders() })
            .then(function (res) {
                if (!res.ok) throw new Error();
                showToast(currentlyActive ? 'Usuario desactivado' : 'Usuario reactivado');
                loadUsers();
            })
            .catch(function () { showToast('Error al cambiar estado', true); });
    }

    /* ── Edit ── */
    function editUser(id) {
        fetch(API + '/' + id, { headers: apiHeaders() })
            .then(function (res) { return res.json(); })
            .then(function (data) { openModal(true, data); })
            .catch(function () { showToast('Error al cargar usuario', true); });
    }

    /* ── Render table ── */
    function renderUsers(users) {
        if (!users.length) {
            tableWrap.style.display = 'none';
            emptyState.style.display = '';
            countEl.textContent = '0 registros';
            return;
        }

        tableWrap.style.display = '';
        emptyState.style.display = 'none';
        countEl.textContent = users.length + ' registro' + (users.length !== 1 ? 's' : '');

        var activos = 0, inactivos = 0;
        users.forEach(function (u) { u.activo ? activos++ : inactivos++; });
        statTotal.textContent = users.length;
        statActivos.textContent = activos;
        statInactivos.textContent = inactivos;

        var html = '';
        users.forEach(function (u) {
            var initial = u.username.charAt(0).toUpperCase();
            var isActive = u.activo;
            var rowClass = isActive ? '' : ' class="row-inactive"';
            var avatarCls = isActive ? 'usr-avatar' : 'usr-avatar usr-avatar--inactive';
            var rolCls = u.rol === 'ADMIN' ? 'usr-role usr-role--admin' : 'usr-role usr-role--empleado';
            var statusCls = isActive ? 'usr-status usr-status--activo' : 'usr-status usr-status--inactivo';
            var statusText = isActive ? 'Activo' : 'Inactivo';

            var toggleIcon = isActive
                ? '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="m15 9-6 6"/><path d="m9 9 6 6"/></svg>'
                : '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M21.5 2v6h-6"/><path d="M21.34 15.57a10 10 0 1 1-.57-8.38"/></svg>';
            var toggleCls = isActive ? 'usr-action-btn usr-action-btn--danger' : 'usr-action-btn usr-action-btn--restore';
            var toggleTitle = isActive ? 'Desactivar' : 'Reactivar';

            html += '<tr' + rowClass + '>'
                + '<td><div class="usr-cell-name"><div class="' + avatarCls + '">' + initial + '</div><span class="usr-name-text">' + escHtml(u.username) + '</span></div></td>'
                + '<td><span class="' + rolCls + '">' + escHtml(u.rol) + '</span></td>'
                + '<td><span class="' + statusCls + '"><span class="usr-status-dot"></span>' + statusText + '</span></td>'
                + '<td class="td-actions">'
                    + '<button class="usr-action-btn" title="Editar" data-edit="' + u.id + '">'
                        + '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" viewBox="0 0 24 24"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/><path d="m15 5 4 4"/></svg>'
                    + '</button>'
                    + '<button class="' + toggleCls + '" title="' + toggleTitle + '" data-toggle="' + u.id + '" data-active="' + isActive + '">'
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
        if (btn) { editUser(btn.getAttribute('data-edit')); return; }

        btn = e.target.closest('[data-toggle]');
        if (btn) { toggleActivo(btn.getAttribute('data-toggle'), btn.getAttribute('data-active') === 'true'); }
    });

    /* ── Load users ── */
    function loadUsers() {
        loading.style.display = '';
        tableWrap.style.display = 'none';
        emptyState.style.display = 'none';

        fetch(API)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                loading.style.display = 'none';
                renderUsers(data);
            })
            .catch(function () {
                loading.style.display = 'none';
                showToast('Error al cargar usuarios', true);
            });
    }

    /* ── Init ── */
    loadUsers();

})();
