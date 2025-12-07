import { useEffect, useState } from 'react';
import './App.css';

// Тип для состояния пользователя (на клиенте)
type UserState = {
  id: number;
  username: string;
  firstName: string;
} | null;

// Тип для ответа от сервера (чтобы TS знал про .role и .id)
type AuthResponse = {
  id: number;
  tgId: number;
  username: string;
  firstName: string;
  role: string;
  createdAt: string;
};

function App() {
  const [user, setUser] = useState<UserState>(null);
  const [status, setStatus] = useState<string>('Загрузка...');

  useEffect(() => {
    // 1. Проверяем наличие Telegram WebApp
    const tg = window.Telegram?.WebApp;

    if (tg) {
      tg.ready();
      // Пытаемся получить реального пользователя
      const tgUser = tg.initDataUnsafe?.user;

      if (tgUser) {
        setUser({
          id: tgUser.id,
          username: tgUser.username || 'Аноним',
          firstName: tgUser.first_name
        });
        setStatus('Готово к регистрации (Telegram)');
        return; // Выходим, если нашли реального пользователя
      }
    }

    // 2. Если мы здесь — значит это браузер (режим разработки)
    console.log("Telegram не найден. Включаем режим отладки.");
    setUser({ id: 12345, username: 'test_dev', firstName: 'Developer' });
    setStatus('Режим разработки (Mock Data)');

  }, []);

  const handleRegister = async () => {
    if (!user) return;
    setStatus('Отправка данных на сервер...');

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          tgId: user.id,
          username: user.username,
          firstName: user.firstName
        }),
      });

      if (response.ok) {
        const data = (await response.json()) as AuthResponse;
        console.log("Ответ от сервера:", data);
        setStatus(`Успех! Вы зарегистрированы как ${data.role}. ID в базе: ${data.id}`);
      } else {
        setStatus('Ошибка сервера!');
      }
    } catch (e) {
      console.error(e);
      setStatus('Ошибка сети. Бэкенд запущен?');
    }
  };

  return (
    <div className="card">
      <h1>Chall X Bot</h1>
      <div style={{ fontSize: '40px', margin: '20px' }}>🤖</div>

      <div className="content">
        <p>Статус: <strong>{status}</strong></p>

        {user && (
          <div style={{ marginTop: '20px', padding: '15px', border: '1px solid #444', borderRadius: '8px', background: '#222', color: '#fff' }}>
            <p style={{ margin: '5px 0' }}>ID: {user.id}</p>
            <p style={{ margin: '5px 0' }}>Ник: @{user.username}</p>
            <button
              onClick={handleRegister}
              style={{ marginTop: '15px', padding: '10px 20px', cursor: 'pointer', fontSize: '16px', background: '#646cff', color: 'white', border: 'none', borderRadius: '4px' }}
            >
              Войти в систему
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;