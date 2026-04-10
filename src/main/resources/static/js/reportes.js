/* ============================================================
   REPORTES — Download PDF / Excel
   ============================================================ */
(function () {
    'use strict';

    var form     = document.getElementById('rptForm');
    var btnPdf   = document.getElementById('btnPdf');
    var btnExcel = document.getElementById('btnExcel');
    var btnClear = document.getElementById('btnClear');
    var toast    = document.getElementById('toast');

    if (!form) return;

    /* ── Toast ── */
    var toastTimer;
    function showToast(msg, isError) {
        clearTimeout(toastTimer);
        toast.textContent = msg;
        toast.className = 'rpt-toast show' + (isError ? ' rpt-toast--error' : '');
        toastTimer = setTimeout(function () { toast.className = 'rpt-toast'; }, 3500);
    }

    /* ── Build query string from form ── */
    function getQueryString() {
        var data = new FormData(form);
        var params = new URLSearchParams();
        data.forEach(function (value, key) {
            if (value.trim() !== '') params.set(key, value.trim());
        });
        return params.toString();
    }

    /* ── Download helper ── */
    function download(format) {
        var qs = getQueryString();
        var url = '/generar-reportes/' + format + (qs ? '?' + qs : '');

        var btn = format === 'pdf' ? btnPdf : btnExcel;
        var origText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="rpt-spinner"></span> Generando…';

        fetch(url)
            .then(function (res) {
                if (!res.ok) throw new Error('Error al generar el reporte');
                return res.blob();
            })
            .then(function (blob) {
                var a = document.createElement('a');
                a.href = URL.createObjectURL(blob);
                var disposition = '';
                var ext = format === 'pdf' ? '.pdf' : '.xlsx';
                a.download = 'reporte_oficios' + ext;
                document.body.appendChild(a);
                a.click();
                a.remove();
                URL.revokeObjectURL(a.href);
                showToast('Reporte descargado correctamente');
            })
            .catch(function (err) {
                showToast(err.message || 'Error al descargar', true);
            })
            .finally(function () {
                btn.disabled = false;
                btn.innerHTML = origText;
            });
    }

    btnPdf.addEventListener('click', function () { download('pdf'); });
    btnExcel.addEventListener('click', function () { download('excel'); });

    btnClear.addEventListener('click', function () {
        form.reset();
        showToast('Filtros limpiados');
    });

})();
