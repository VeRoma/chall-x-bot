import { useEffect, useState } from 'react';
import './App.css';
import type { User, Topic } from './types';
import { TopicsScreen } from './screens/TopicsScreen';
import { LessonScreen } from './screens/LessonScreen';
import { QuizScreen } from './screens/QuizScreen';
import { Loader } from './components/Loader';

// Возможные состояния (экраны) приложения
type ViewState = 'loading' | 'topics' | 'lesson' | 'quiz' | 'word-quiz';

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [view, setView] = useState<ViewState>('loading');
  const [status, setStatus] = useState('Загрузка...');

  // Храним выбранную тему, чтобы передать её в урок или тест
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);

  // --- 1. Инициализация и Вход ---
  useEffect(() => {
    const tg = window.Telegram?.WebApp;

    const initApp = async () => {
      if (tg) {
        tg.ready();
        try { tg.expand(); } catch { }

        // Настройка цветов (с исправлением ошибки TypeScript)
        const theme = tg.themeParams;
        if (theme) {
          document.documentElement.style.setProperty('--tg-theme-bg-color', tg.backgroundColor || theme.bg_color || '#fff');
          document.documentElement.style.setProperty('--tg-theme-text-color', theme.text_color || '#000');
          document.documentElement.style.setProperty('--tg-theme-button-color', theme.button_color || '#3390ec');
          document.documentElement.style.setProperty('--tg-theme-button-text-color', theme.button_text_color || '#fff');
        }

        const u = tg.initDataUnsafe?.user;

        // Если зашли через Telegram -> Авто-логин
        if (u) {
          try {
            const res = await fetch('/api/auth/login', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({
                tgId: u.id,
                username: u.username || '',
                firstName: u.first_name
              })
            });

            if (res.ok) {
              const userData = await res.json();
              setUser(userData);
              setView('topics'); // Сразу показываем темы
            } else {
              setStatus('Ошибка входа в систему');
            }
          } catch (e) {
            setStatus('Нет соединения с сервером');
          }
        } else {
          // Если зашли через браузер (dev mode)
          console.log("Dev mode: Fake user login");
          setUser({ id: 123, username: 'dev', firstName: 'Developer' });
          setView('topics');
        }
      } else {
        // Fallback для браузера без объекта Telegram
        setUser({ id: 123, username: 'dev', firstName: 'Developer' });
        setView('topics');
      }
    };

    initApp();
  }, []);

  // --- 2. Навигация ---

  const goHome = () => {
    setView('topics');
    setSelectedTopic(null);
  };

  const handleTopicClick = (topic: Topic) => {
    setSelectedTopic(topic);
    setView('lesson');
  };

  const startGrammarQuiz = () => {
    if (selectedTopic) setView('quiz');
  };

  const startWordChallenge = () => {
    setView('word-quiz');
  };

  // --- 3. Рендер Экранов ---

  if (view === 'loading') {
    return <Loader text={status} />;
  }

  if (view === 'topics') {
    return (
      <TopicsScreen
        user={user}
        onTopicClick={handleTopicClick}
        onStartChallenge={startWordChallenge}
      />
    );
  }

  if (view === 'lesson' && selectedTopic) {
    return (
      <LessonScreen
        topic={selectedTopic}
        onBack={goHome}
        onStartQuiz={startGrammarQuiz}
      />
    );
  }

  if (view === 'quiz' && selectedTopic) {
    return (
      <QuizScreen
        mode="grammar"
        topic={selectedTopic}
        onBack={goHome}
      />
    );
  }

  if (view === 'word-quiz') {
    return (
      <QuizScreen
        mode="word"
        onBack={goHome}
      />
    );
  }

  return <div>Ошибка: Неизвестное состояние</div>;
}

export default App;