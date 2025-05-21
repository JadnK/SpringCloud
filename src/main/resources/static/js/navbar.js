document.addEventListener('DOMContentLoaded', () => {
  const usernameBox = document.getElementById('usernameBox');
  const dropdown = document.getElementById('logoutDropdown');

  usernameBox.addEventListener('click', (e) => {
    e.stopPropagation();
    dropdown.classList.toggle('show');
  });

  document.addEventListener('click', () => {
    dropdown.classList.remove('show');
  });

  dropdown.addEventListener('click', e => {
    e.stopPropagation();
  });
});
