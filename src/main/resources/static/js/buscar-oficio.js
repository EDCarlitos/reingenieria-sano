/* ============================================================
   BUSCAR OFICIO — Client-side search via API
   ============================================================ */

(function () {
    'use strict';

    /* ── DOM refs ── */
    var filterCard     = document.getElementById('filterCard');
    var filterHeader   = document.getElementById('filterCardHeader');
    var filterToggle   = document.getElementById('filterToggle');

    var filterCount    = document.getElementById('filterCount');
    var chipsBar       = document.getElementById('chipsBar');
    var chipsList      = document.getElementById('chipsList');

    var btnSearch      = document.getElementById('btnSearch');
    var btnClear       = document.getElementById('btnClear');

    var rpCount        = document.getElementById('rpCount');
    var rpLoading      = document.getElementById('rpLoading');
    var rpTableWrap    = document.getElementById('rpTableWrap');
    var rpTbody        = document.getElementById('rpTbody');
    var rpEmpty        = document.getElementById('rpEmpty');
    var rpPagination   = document.getElementById('rpPagination');
    var rpPagInfo      = document.getElementById('rpPagInfo');
    var rpPagControls  = document.getElementById('rpPagControls');

    var PAGE_SIZE = 15;
    var currentPage = 0;

    /* ── Filter field definitions ── */
    var fields = [
        { id: 'f-paterno',       param: 'paterno',           label: 'Paterno' },
        { id: 'f-materno',       param: 'materno',           label: 'Materno' },
        { id: 'f-nombres',       param: 'nombres',           label: 'Nombres' },
        { id: 'f-asunto',        param: 'asunto',            label: 'Asunto' },
        { id: 'f-funcionario',   param: 'funcionarioNombre', label: 'Funcionario' },
        { id: 'f-tipo',          param: 'esRespuesta',       label: 'Tipo' },
        { id: 'f-fechaDesde',    param: 'fechaDesde',        label: 'Desde' },
        { id: 'f-fechaHasta',    param: 'fechaHasta',        label: 'Hasta' }
    ];

    /* ── Helpers ── */
    function getFilterValues() {
        var values = {};
        fields.forEach(function (f) {
            var el = document.getElementById(f.id);
            if (el && el.value.trim() !== '') {
                values[f.param] = el.value.trim();
            }
        });
        return values;
    }

    function countActiveFilters() {
        return Object.keys(getFilterValues()).length;
    }

    function updateFilterBadge() {
        var n = countActiveFilters();
        if (n > 0) {
            filterCount.textContent = n;
            filterCount.style.display = '';
        } else {
            filterCount.style.display = 'none';
        }
    }

    function renderChips(filterValues) {
        chipsList.innerHTML = '';
        var labels = {};
        fields.forEach(function (f) { labels[f.param] = f.label; });

        var keys = Object.keys(filterValues);
        if (keys.length === 0) {
            chipsBar.style.display = 'none';
            return;
        }
        chipsBar.style.display = '';

        keys.forEach(function (key, i) {
            var val = filterValues[key];
            if (key === 'esRespuesta') {
                val = val === 'true' ? 'En contestación' : 'Original';
            }
            var chip = document.createElement('span');
            chip.className = 'chip';
            chip.style.animationDelay = (i * 0.04) + 's';
            chip.innerHTML = labels[key] + ': <b>' + escapeHtml(val) + '</b>';
            chipsList.appendChild(chip);
        });
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

    /* ── Show / hide sections ── */
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

        /* Count badge */
        var n = data.totalElements;
        rpCount.textContent = n + (n === 1 ? ' registro' : ' registros');

        /* Build rows */
        rpTbody.innerHTML = '';
        data.content.forEach(function (o, idx) {
            var tr = document.createElement('tr');
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
                '<td><span class="tipo-tag ' + tipoClass + '">' + tipoLabel + '</span></td>';

            rpTbody.appendChild(tr);
        });

        rpTableWrap.style.display = '';

        /* Pagination */
        if (data.totalPages > 1) {
            renderPagination(data);
            rpPagination.style.display = '';
        } else {
            rpPagination.style.display = 'none';
        }
    }

    /* ── Pagination renderer ── */
    function renderPagination(data) {
        rpPagInfo.innerHTML =
            'Página <strong>' + (data.currentPage + 1) + '</strong> de <strong>' +
            data.totalPages + '</strong> — <strong>' + data.totalElements + '</strong> registros';

        rpPagControls.innerHTML = '';

        /* Prev */
        var prev = makePageBtn('‹', data.currentPage - 1, !data.hasPrevious);
        rpPagControls.appendChild(prev);

        /* Page numbers (window of 7) */
        var total = data.totalPages;
        var cur   = data.currentPage;
        var start = 0;
        var end   = total;

        if (total > 7) {
            if (cur <= 3) {
                start = 0; end = 7;
            } else if (cur >= total - 4) {
                start = total - 7; end = total;
            } else {
                start = cur - 3; end = cur + 4;
            }
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

        /* Next */
        var next = makePageBtn('›', data.currentPage + 1, !data.hasNext);
        rpPagControls.appendChild(next);
    }

    function makePageBtn(text, page, disabled) {
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'pg-btn';
        btn.textContent = text;
        btn.disabled = disabled;
        if (!disabled) {
            btn.addEventListener('click', function () {
                currentPage = page;
                doSearch();
            });
        }
        return btn;
    }

    function makeDots() {
        var span = document.createElement('span');
        span.className = 'pg-dots';
        span.textContent = '…';
        return span;
    }

    /* ── API call ── */
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
                if (!data.content || data.content.length === 0) {
                    showEmpty();
                } else {
                    showResults(data);
                }
            })
            .catch(function () {
                showEmpty();
            });
    }

    /* ── Events ── */

    /* Toggle filter panel */
    filterHeader.addEventListener('click', function () {
        filterCard.classList.toggle('collapsed');
    });
    filterToggle.addEventListener('click', function (e) {
        e.stopPropagation();
        filterCard.classList.toggle('collapsed');
    });

    /* Search */
    btnSearch.addEventListener('click', function () {
        currentPage = 0;
        doSearch();
    });

    /* Enter key on any filter input */
    document.querySelectorAll('.fg input, .fg select').forEach(function (el) {
        el.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                currentPage = 0;
                doSearch();
            }
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
