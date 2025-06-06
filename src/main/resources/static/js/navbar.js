document.addEventListener('DOMContentLoaded', () => {
  const usernameBox = document.getElementById('username-box');
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
