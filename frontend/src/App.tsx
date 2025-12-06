import { useEffect, useState } from 'react';
import './App.css';

// Тип для состояния пользователя
type UserState = {
  id: number;
  username: string;
  firstName: string;
} | null;

function App() {
  const [user, setUser] = useState<UserState>(null);
  const [status, setStatus] = useState<string>('Загрузка...');

  useEffect(() => {
    // Пытаемся получить объект Telegram WebApp
    const tg = window.Telegram?.WebApp;

    if (tg) {
      tg.ready(); // Сообщаем Телеграму, что приложение готово

      // В реальном Telegram здесь будут данные. 
      // В обычном браузере initDataUnsafe.user будет undefined.
      const tgUser = tg.initDataUnsafe?.user;

      if (tgUser) {
        setUser({
          id: tgUser.id,
          username: tgUser.username || 'Аноним',
          firstName: tgUser.first_name
        });
        setStatus('Готово к регистрации');
      } else {
        // Режим разработки в браузере (Fallback)
        // Чтобы мы могли верстать, не запуская каждый раз в Телеграме
        console.log("Telegram user not found, using mock data.");
        setUser({ id: 12345, username: 'test_dev', firstName: 'Developer' });
        setStatus('Режим разработки (Mock Data)');
      }
    }
  }, []);

  const handleRegister = async () => {
    if (!user) return;
    setStatus('Отправка данных на сервер...');

    console.log("Отправляем на бэкенд:", user);

    // Имитация задержки сети
    setTimeout(() => {
      setStatus(`Успех! Привет, ${user.firstName}.`);
    }, 1000);
  };

  return (
    <div className="card">
      <h1>Chall X Bot</h1>
      {/* Место для логотипа */}
      <div style={{ fontSize: '40px', margin: '20px' }}>🤖</div>

      <div className="content">
        <p>Статус: <strong>{status}</strong></p>

        {user && (
          <div style={{ marginTop: '20px', padding: '10px', border: '1px solid #ccc', borderRadius: '8px' }}>
            <p>ID: {user.id}</p>
            <p>Ник: @{user.username}</p>
            <button onClick={handleRegister} style={{ marginTop: '10px', padding: '8px 16px', cursor: 'pointer' }}>
              Войти в систему
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;