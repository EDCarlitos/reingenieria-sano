/* ============================================================
   LOGS — Client-side list + filters
   ============================================================ */
(function () {
    'use strict';

    /* ── DOM refs ── */
    var tbody       = document.getElementById('logTbody');
    var tableWrap   = document.getElementById('logTableWrap');
    var emptyState  = document.getElementById('logEmpty');
    var loading     = document.getElementById('logLoading');
    var countEl     = document.getElementById('logCount');

    var statTotal    = document.getElementById('statTotal');
    var statCrear    = document.getElementById('statCrear');
    var statEditar   = document.getElementById('statEditar');
    var statEliminar = document.getElementById('statEliminar');

    var filterAccion  = document.getElementById('filterAccion');
    var filterEntidad = document.getElementById('filterEntidad');
    var filterUsuario = document.getElementById('filterUsuario');
    var btnClear      = document.getElementById('btnClear');

    var API = '/logs/api';

    var allLogs = [];

    /* ── Load ── */
    function loadLogs() {
        loading.style.display = '';
        tableWrap.style.display = 'none';
        emptyState.style.display = 'none';

        fetch(API)
            .then(function (res) { return res.json(); })
            .then(function (data) {
                allLogs = data;
                updateStats(data);
                applyFilters();
            })
            .catch(function () {
                allLogs = [];
                renderLogs([]);
            })
            .finally(function () {
                loading.style.display = 'none';
            });
    }

    /* ── Stats ── */
    function updateStats(logs) {
        statTotal.textContent = logs.length;
        statCrear.textContent = logs.filter(function (l) { return l.accion === 'CREAR'; }).length;
        statEditar.textContent = logs.filter(function (l) { return l.accion === 'EDITAR'; }).length;
        statEliminar.textContent = logs.filter(function (l) { return l.accion === 'ELIMINAR'; }).length;
    }

    /* ── Filters ── */
    function applyFilters() {
        var accion  = filterAccion.value;
        var entidad = filterEntidad.value;
        var usuario = filterUsuario.value.trim().toLowerCase();

        var filtered = allLogs.filter(function (l) {
            if (accion && l.accion !== accion) return false;
            if (entidad && l.entidad !== entidad) return false;
            if (usuario && l.usuario.toLowerCase().indexOf(usuario) === -1) return false;
            return true;
        });

        renderLogs(filtered);
    }

    filterAccion.addEventListener('change', applyFilters);
    filterEntidad.addEventListener('change', applyFilters);

    var searchTimer;
    filterUsuario.addEventListener('input', function () {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(applyFilters, 250);
    });

    btnClear.addEventListener('click', function () {
        filterAccion.value = '';
        filterEntidad.value = '';
        filterUsuario.value = '';
        applyFilters();
    });

    /* ── Render ── */
    function renderLogs(logs) {
        if (!logs.length) {
            tableWrap.style.display = 'none';
            emptyState.style.display = '';
            countEl.textContent = '0 registros';
            return;
        }

        tableWrap.style.display = '';
        emptyState.style.display = 'none';
        countEl.textContent = logs.length + ' registro' + (logs.length !== 1 ? 's' : '');

        var html = '';
        for (var i = 0; i < logs.length; i++) {
            var log = logs[i];
            var fechaStr = formatFecha(log.fecha);

            html += '<tr>'
                + '<td class="log-fecha">'
                +   '<div class="log-fecha-date">' + fechaStr.date + '</div>'
                +   '<div class="log-fecha-time">' + fechaStr.time + '</div>'
                + '</td>'
                + '<td>'
                +   '<div class="log-user-cell">'
                +     '<div class="log-user-avatar">' + escapeHtml(log.usuario.charAt(0).toUpperCase()) + '</div>'
                +     '<span class="log-user-name">' + escapeHtml(log.usuario) + '</span>'
                +   '</div>'
                + '</td>'
                + '<td><span class="log-accion log-accion--' + escapeHtml(log.accion) + '">' + escapeHtml(log.accion) + '</span></td>'
                + '<td><span class="log-entidad">' + escapeHtml(log.entidad) + '</span></td>'
                + '<td class="log-detalle">' + escapeHtml(log.detalle || '') + '</td>'
                + '</tr>';
        }

        tbody.innerHTML = html;
    }

    /* ── Helpers ── */
    function formatFecha(fecha) {
        if (!fecha) return { date: '—', time: '' };
        // fecha comes as ISO array [2025,4,9,14,30,0] or ISO string
        var d;
        if (Array.isArray(fecha)) {
            d = new Date(fecha[0], fecha[1] - 1, fecha[2], fecha[3] || 0, fecha[4] || 0, fecha[5] || 0);
        } else {
            d = new Date(fecha);
        }
        if (isNaN(d.getTime())) return { date: '—', time: '' };

        var day = pad(d.getDate());
        var month = pad(d.getMonth() + 1);
        var year = d.getFullYear();
        var hours = pad(d.getHours());
        var mins = pad(d.getMinutes());

        return {
            date: day + '/' + month + '/' + year,
            time: hours + ':' + mins
        };
    }

    function pad(n) {
        return n < 10 ? '0' + n : '' + n;
    }

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    /* ── Init ── */
    loadLogs();

})();
