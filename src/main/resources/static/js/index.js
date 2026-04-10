/* ============================================================
   INDEX — Search, Filter, Pagination + Edit & Delete
   ============================================================ */
(function () {
    'use strict';

    /* ── DOM refs: filters ── */
    var filterCard     = document.getElementById('filterCard');
    var filterHeader   = document.getElementById('filterCardHeader');
    var filterToggle   = document.getElementById('filterToggle');
    var filterCount    = document.getElementById('filterCount');
    var chipsBar       = document.getElementById('chipsBar');
    var chipsList      = document.getElementById('chipsList');
    var btnSearch      = document.getElementById('btnSearch');
    var btnClear       = document.getElementById('btnClear');

    /* ── DOM refs: results ── */
    var rpCount        = document.getElementById('rpCount');
    var rpLoading      = document.getElementById('rpLoading');
    var rpTableWrap    = document.getElementById('rpTableWrap');
    var rpTbody        = document.getElementById('rpTbody');
    var rpEmpty        = document.getElementById('rpEmpty');
    var rpPagination   = document.getElementById('rpPagination');
    var rpPagInfo      = document.getElementById('rpPagInfo');
    var rpPagControls  = document.getElementById('rpPagControls');

    /* ── DOM refs: modals ── */
    var modalEditar    = document.getElementById('modalEditar');
    var modalEliminar  = document.getElementById('modalEliminar');
    var formEditar     = document.getElementById('formEditar');
    var formEliminar   = document.getElementById('formEliminar');

    if (!filterCard || !rpTbody) return;

    var PAGE_SIZE   = 15;
    var currentPage = 0;
    var isAdmin     = window.IS_ADMIN || false;

    /* In-memory map of oficios from current results page */
    var oficiosMap = {};

    /* ── Filter field definitions ── */
    var fields = [
        { id: 'f-paterno',     param: 'paterno',           label: 'Paterno' },
        { id: 'f-materno',     param: 'materno',           label: 'Materno' },
        { id: 'f-nombres',     param: 'nombres',           label: 'Nombres' },
        { id: 'f-asunto',      param: 'asunto',            label: 'Asunto' },
        { id: 'f-funcionario', param: 'funcionarioNombre', label: 'Funcionario' },
        { id: 'f-tipo',        param: 'esRespuesta',       label: 'Tipo' },
        { id: 'f-fechaDesde',  param: 'fechaDesde',        label: 'Desde' },
        { id: 'f-fechaHasta',  param: 'fechaHasta',        label: 'Hasta' }
    ];

    /* ═══════════════════════════════════════════
       HELPERS
    ═══════════════════════════════════════════ */

    function getCsrfToken() {
        var match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        return match ? decodeURIComponent(match[1]) : '';
    }

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    function formatDate(isoStr) {
        if (!isoStr) return '—';
        var parts = isoStr.split('-');
        if (parts.length === 3) return parts[2] + '/' + parts[1] + '/' + parts[0];
        return isoStr;
    }

    /* ── Toast notification ── */
    function showToast(message, type) {
        var existing = document.querySelector('.toast');
        if (existing) existing.remove();
        var toast = document.createElement('div');
        toast.className = 'toast toast--' + type;
        toast.textContent = message;
        document.body.appendChild(toast);
        requestAnimationFrame(function () { toast.classList.add('toast--visible'); });
        setTimeout(function () {
            toast.classList.remove('toast--visible');
            setTimeout(function () { toast.remove(); }, 300);
        }, 3500);
    }

    /* ── Modal management ── */
    function openModal(modal) {
        if (!modal) return;
        modal.style.display = 'flex';
        requestAnimationFrame(function () { modal.classList.add('modal--active'); });
    }

    function closeModal(modal) {
        if (!modal) return;
        modal.classList.remove('modal--active');
        setTimeout(function () { modal.style.display = 'none'; }, 250);
    }

    document.querySelectorAll('.modal-overlay').forEach(function (overlay) {
        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) closeModal(overlay);
        });
    });

    document.querySelectorAll('.modal-close, .modal-close-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var modal = btn.closest('.modal-overlay');
            if (modal) closeModal(modal);
        });
    });

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.modal--active').forEach(function (m) {
                closeModal(m);
            });
        }
    });

    async function parseError(res, defaultMsg) {
        try {
            var data = await res.json();
            return data.error || data.message || defaultMsg;
        } catch (_) {
            try { return (await res.text()) || defaultMsg; } catch (__) { return defaultMsg; }
        }
    }

    /* ═══════════════════════════════════════════
       FILTER LOGIC
    ═══════════════════════════════════════════ */

    function getFilterValues() {
        var values = {};
        fields.forEach(function (f) {
            var el = document.getElementById(f.id);
            if (el && el.value.trim() !== '') values[f.param] = el.value.trim();
        });
        return values;
    }

    function updateFilterBadge() {
        var n = Object.keys(getFilterValues()).length;
        if (n > 0) { filterCount.textContent = n; filterCount.style.display = ''; }
        else { filterCount.style.display = 'none'; }
    }

    function renderChips(filterValues) {
        chipsList.innerHTML = '';
        var labels = {};
        fields.forEach(function (f) { labels[f.param] = f.label; });
        var keys = Object.keys(filterValues);
        if (keys.length === 0) { chipsBar.style.display = 'none'; return; }
        chipsBar.style.display = '';
        keys.forEach(function (key, i) {
            var val = filterValues[key];
            if (key === 'esRespuesta') val = val === 'true' ? 'En contestación' : 'Original';
            var chip = document.createElement('span');
            chip.className = 'chip';
            chip.style.animationDelay = (i * 0.04) + 's';
            chip.innerHTML = labels[key] + ': <b>' + escapeHtml(val) + '</b>';
            chipsList.appendChild(chip);
        });
    }

    /* ═══════════════════════════════════════════
       SHOW / HIDE SECTIONS
    ═══════════════════════════════════════════ */

    function showLoading() {
        rpLoading.style.display = '';
        rpTableWrap.style.display = 'none';
        rpEmpty.style.display = 'none';
        rpPagination.style.display = 'none';
    }

    function showEmpty() {
        rpLoading.style.display = 'none';
        rpTableWrap.style.display = 'none';
        rpEmpty.style.display = '';
        rpPagination.style.display = 'none';
        rpCount.textContent = '0 registros';
    }

    function showResults(data) {
        rpLoading.style.display = 'none';
        rpEmpty.style.display = 'none';

        var n = data.totalElements;
        rpCount.textContent = n + (n === 1 ? ' registro' : ' registros');

        /* Rebuild oficiosMap from current page */
        oficiosMap = {};
        data.content.forEach(function (o) { oficiosMap[o.id] = o; });

        /* Build rows */
        rpTbody.innerHTML = '';
        data.content.forEach(function (o, idx) {
            var tr = document.createElement('tr');
            tr.setAttribute('data-oficio-id', o.id);
            tr.style.animationDelay = (idx * 0.03) + 's';

            var solicitante = escapeHtml((o.paterno || '') + ' ' + (o.materno || '') + ', ' + (o.nombres || ''));
            var funcionario = o.funcionario ? escapeHtml(o.funcionario.nombre) : '—';
            var fecha       = formatDate(o.fecha);
            var esResp      = o.esRespuesta === true;
            var tipoClass   = esResp ? 'respuesta' : 'original';
            var tipoLabel   = esResp ? 'En contestación' : 'Original';

            tr.innerHTML =
                '<td><span class="oficio-badge">' + escapeHtml(o.id || '—') + '</span></td>' +
                '<td>' + solicitante + '</td>' +
                '<td class="asunto-cell" title="' + escapeHtml(o.asunto || '') + '">' + escapeHtml(o.asunto || '—') + '</td>' +
                '<td>' + funcionario + '</td>' +
                '<td>' + fecha + '</td>' +
                '<td><span class="tipo-tag ' + tipoClass + '">' + tipoLabel + '</span></td>' +
                '<td>' +
                    '<div class="actions-cell">' +
                        '<button class="btn-action btn-edit" data-id="' + escapeHtml(o.id) + '" title="Editar">' +
                            '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>' +
                        '</button>' +
                        '<button class="btn-action btn-delete" data-id="' + escapeHtml(o.id) + '" title="Eliminar">' +
                            '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>' +
                        '</button>' +
                    '</div>' +
                '</td>';

            rpTbody.appendChild(tr);
        });

        /* Attach edit/delete handlers to new buttons */
        rpTbody.querySelectorAll('.btn-edit').forEach(function (btn) {
            btn.addEventListener('click', function () { handleEdit(btn.dataset.id); });
        });
        rpTbody.querySelectorAll('.btn-delete').forEach(function (btn) {
            btn.addEventListener('click', function () { handleDelete(btn.dataset.id); });
        });

        rpTableWrap.style.display = '';

        if (data.totalPages > 1) {
            renderPagination(data);
            rpPagination.style.display = '';
        } else {
            rpPagination.style.display = 'none';
        }
    }

    /* ═══════════════════════════════════════════
       PAGINATION
    ═══════════════════════════════════════════ */

    function renderPagination(data) {
        rpPagInfo.innerHTML =
            'Página <strong>' + (data.currentPage + 1) + '</strong> de <strong>' +
            data.totalPages + '</strong> — <strong>' + data.totalElements + '</strong> registros';
        rpPagControls.innerHTML = '';

        rpPagControls.appendChild(makePageBtn('‹', data.currentPage - 1, !data.hasPrevious));

        var total = data.totalPages;
        var cur   = data.currentPage;
        var start = 0, end = total;

        if (total > 7) {
            if (cur <= 3)            { start = 0;         end = 7; }
            else if (cur >= total-4) { start = total - 7; end = total; }
            else                     { start = cur - 3;   end = cur + 4; }
        }

        if (total > 7 && start > 0) {
            rpPagControls.appendChild(makePageBtn('1', 0, false));
            if (start > 1) rpPagControls.appendChild(makeDots());
        }
        for (var i = start; i < end; i++) {
            var btn = makePageBtn(String(i + 1), i, false);
            if (i === cur) btn.classList.add('active');
            rpPagControls.appendChild(btn);
        }
        if (total > 7 && end < total) {
            if (end < total - 1) rpPagControls.appendChild(makeDots());
            rpPagControls.appendChild(makePageBtn(String(total), total - 1, false));
        }

        rpPagControls.appendChild(makePageBtn('›', data.currentPage + 1, !data.hasNext));
    }

    function makePageBtn(text, page, disabled) {
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'pg-btn';
        btn.textContent = text;
        btn.disabled = disabled;
        if (!disabled) {
            btn.addEventListener('click', function () { currentPage = page; doSearch(); });
        }
        return btn;
    }

    function makeDots() {
        var span = document.createElement('span');
        span.className = 'pg-dots';
        span.textContent = '…';
        return span;
    }

    /* ═══════════════════════════════════════════
       API SEARCH
    ═══════════════════════════════════════════ */

    function doSearch() {
        var filterValues = getFilterValues();
        updateFilterBadge();
        renderChips(filterValues);
        showLoading();

        var params = new URLSearchParams(filterValues);
        params.set('page', currentPage);
        params.set('size', PAGE_SIZE);

        fetch('/api/oficios?' + params.toString())
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (data) {
                if (!data.content || data.content.length === 0) showEmpty();
                else showResults(data);
            })
            .catch(function () { showEmpty(); });
    }

    /* ═══════════════════════════════════════════
       EDIT / DELETE HANDLERS
    ═══════════════════════════════════════════ */

    function handleEdit(id) {
        var oficio = oficiosMap[id];
        if (!oficio || !modalEditar) return;

        document.getElementById('editOficioId').value = oficio.id;
        document.getElementById('editPaterno').value  = oficio.paterno || '';
        document.getElementById('editMaterno').value  = oficio.materno || '';
        document.getElementById('editNombres').value  = oficio.nombres || '';
        document.getElementById('editContesta').value = oficio.contesta || '';
        document.getElementById('editEsRespuesta').checked = oficio.esRespuesta;
        document.getElementById('editAsunto').value      = oficio.asunto || '';
        document.getElementById('editObservacion').value = oficio.observacion || '';

        var funcSelect = document.getElementById('editFuncionarioId');
        funcSelect.value = (oficio.funcionario && oficio.funcionario.id) ? oficio.funcionario.id : '';

        openModal(modalEditar);
    }

    function handleDelete(id) {
        var oficio = oficiosMap[id];
        if (!oficio || !modalEliminar) return;

        document.getElementById('deleteOficioId').value   = id;
        document.getElementById('deleteOficioNum').textContent  = '#' + oficio.id;
        document.getElementById('deleteOficioName').textContent =
            (oficio.nombres || '') + ' ' + (oficio.paterno || '') + ' ' + (oficio.materno || '');

        var adminGroup    = document.getElementById('adminUsernameGroup');
        var passwordLabel = document.getElementById('deletePasswordLabel');
        if (isAdmin) {
            adminGroup.style.display = 'none';
            passwordLabel.textContent = 'Tu contraseña *';
        } else {
            adminGroup.style.display = '';
            passwordLabel.textContent = 'Contraseña del administrador *';
        }

        document.getElementById('deleteMotivo').value        = '';
        document.getElementById('deleteAdminUsername').value  = '';
        document.getElementById('deletePassword').value      = '';

        openModal(modalEliminar);
    }

    /* ── Edit form submit ── */
    if (formEditar) {
        formEditar.addEventListener('submit', async function (e) {
            e.preventDefault();
            var submitBtn = formEditar.querySelector('button[type="submit"]');
            submitBtn.disabled = true;

            var id   = document.getElementById('editOficioId').value;
            var body = {
                paterno:       document.getElementById('editPaterno').value,
                materno:       document.getElementById('editMaterno').value,
                nombres:       document.getElementById('editNombres').value,
                contesta:      document.getElementById('editContesta').value,
                esRespuesta:   document.getElementById('editEsRespuesta').checked,
                asunto:        document.getElementById('editAsunto').value,
                observacion:   document.getElementById('editObservacion').value,
                funcionarioId: document.getElementById('editFuncionarioId').value
            };

            try {
                var res = await fetch('/api/oficios/' + encodeURIComponent(id), {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken() },
                    body: JSON.stringify(body)
                });
                if (!res.ok) throw new Error(await parseError(res, 'Error al actualizar'));

                closeModal(modalEditar);
                showToast('Oficio actualizado correctamente', 'success');
                setTimeout(function () { doSearch(); }, 600);
            } catch (err) {
                showToast(err.message || 'Error al actualizar el oficio', 'error');
            } finally {
                submitBtn.disabled = false;
            }
        });
    }

    /* ── Delete form submit ── */
    if (formEliminar) {
        formEliminar.addEventListener('submit', async function (e) {
            e.preventDefault();
            var submitBtn = formEliminar.querySelector('button[type="submit"]');
            submitBtn.disabled = true;

            var id   = document.getElementById('deleteOficioId').value;
            var body = {
                motivoEliminacion: document.getElementById('deleteMotivo').value,
                password:          document.getElementById('deletePassword').value
            };
            if (!isAdmin) {
                body.username = document.getElementById('deleteAdminUsername').value;
                if (!body.username) {
                    showToast('Ingrese el usuario administrador', 'error');
                    submitBtn.disabled = false;
                    return;
                }
            }
            if (!body.motivoEliminacion) {
                showToast('Ingrese el motivo de eliminación', 'error');
                submitBtn.disabled = false;
                return;
            }

            try {
                var res = await fetch('/api/oficios/' + encodeURIComponent(id) + '/eliminar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'X-XSRF-TOKEN': getCsrfToken() },
                    body: JSON.stringify(body)
                });
                if (!res.ok) throw new Error(await parseError(res, 'Error al eliminar'));

                closeModal(modalEliminar);
                showToast('Oficio eliminado correctamente', 'success');

                /* Animate row removal then re-search */
                var row = document.querySelector('tr[data-oficio-id="' + id + '"]');
                if (row) {
                    row.style.opacity = '0';
                    row.style.transform = 'translateX(-20px)';
                    setTimeout(function () { doSearch(); }, 400);
                } else {
                    doSearch();
                }
            } catch (err) {
                showToast(err.message || 'Error al eliminar el oficio', 'error');
            } finally {
                submitBtn.disabled = false;
            }
        });
    }

    /* ═══════════════════════════════════════════
       EVENTS
    ═══════════════════════════════════════════ */

    /* Toggle filter panel */
    filterHeader.addEventListener('click', function () { filterCard.classList.toggle('collapsed'); });
    filterToggle.addEventListener('click', function (e) {
        e.stopPropagation();
        filterCard.classList.toggle('collapsed');
    });

    /* Search */
    btnSearch.addEventListener('click', function () { currentPage = 0; doSearch(); });

    /* Enter key on filter inputs */
    document.querySelectorAll('.fg input, .fg select').forEach(function (el) {
        el.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') { e.preventDefault(); currentPage = 0; doSearch(); }
        });
    });

    /* Clear */
    btnClear.addEventListener('click', function () {
        fields.forEach(function (f) {
            var el = document.getElementById(f.id);
            if (el) el.value = '';
        });
        currentPage = 0;
        updateFilterBadge();
        renderChips({});
        showEmpty();
    });

    /* ── Initial load ── */
    doSearch();

})();
