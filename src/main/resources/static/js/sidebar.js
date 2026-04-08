document.addEventListener('DOMContentLoaded', function () {
  var btn = document.getElementById('sidebarToggle');
  var closeBtn = document.getElementById('sidebarClose');
  var overlay = document.getElementById('sidebarOverlay');
  var body = document.body;

  function openSidebar() {
    body.classList.add('sidebar-open');
    if (btn) btn.setAttribute('aria-expanded', 'true');
  }

  function closeSidebar() {
    body.classList.remove('sidebar-open');
    if (btn) btn.setAttribute('aria-expanded', 'false');
  }

  if (btn) btn.addEventListener('click', openSidebar);
  if (closeBtn) closeBtn.addEventListener('click', closeSidebar);
  if (overlay) overlay.addEventListener('click', closeSidebar);

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeSidebar();
  });
});
