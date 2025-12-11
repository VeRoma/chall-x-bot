import { useEffect, useState } from 'react';
import './App.css';

// --- ТИПЫ ДАННЫХ ---
type Topic = { id: number; name: string; isActive?: boolean; active?: boolean; };
type LessonData = { id: number; title: string; content: string; };

// Тип вопроса, который мы ждем от Backend (JSON)
type QuizQuestion = {
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string;
};

// Экраны
type ViewState = 'login' | 'topics' | 'lesson' | 'quiz';

function App() {
  const [user, setUser] = useState<{ firstName: string; id: number; username: string } | null>(null);
  const [status, setStatus] = useState<string>('Загрузка...');

  // Навигация
  const [view, setView] = useState<ViewState>('login');

  // Данные темы и урока
  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);
  const [lessonHtml, setLessonHtml] = useState<string>(''); // Храним HTML урока

  // --- Состояние Квиза ---
  const [quizQuestions, setQuizQuestions] = useState<QuizQuestion[]>([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [score, setScore] = useState(0);
  const [showResult, setShowResult] = useState(false);

  // Состояние ответа: idle (ждем), correct (верно), wrong (ошибка)
  const [answerState, setAnswerState] = useState<'idle' | 'correct' | 'wrong'>('idle');

  // --- ИНИЦИАЛИЗАЦИЯ ---
  useEffect(() => {
    const tg = window.Telegram?.WebApp;
    if (tg) {
      tg.ready();
      try { tg.expand(); } catch { }

      // Темизация
      const applyTheme = () => document.documentElement.setAttribute('data-theme', tg.colorScheme);
      applyTheme();
      tg.onEvent('themeChanged', applyTheme);

      const u = tg.initDataUnsafe?.user;
      if (u) setUser({ id: u.id, username: u.username || '', firstName: u.first_name });
    } else {
      setUser({ id: 123, username: 'dev', firstName: 'Developer' });
    }
  }, []);

  // --- ЗАГРУЗКА ДАННЫХ ---
  const fetchTopics = async () => {
    try {
      const res = await fetch('/api/topics');
      const data = await res.json();
      setTopics(data.length ? data : []);
      setView('topics');
    } catch { setStatus('Ошибка загрузки тем'); }
  };

  const handleTopicClick = async (topic: Topic) => {
    setSelectedTopic(topic);
    setStatus(`Грузим урок...`);
    try {
      const res = await fetch(`/api/lessons/by-topic/${topic.id}`);
      if (res.ok) {
        const data: LessonData = await res.json();
        // Присваиваем HTML напрямую
        setLessonHtml(`<h2>${data.title}</h2><br/>${data.content}`);
        setView('lesson');
      }
    } catch {
      setLessonHtml('<h3>Ошибка загрузки урока</h3>');
      setView('lesson');
    }
  };

  // --- ЛОГИКА КВИЗА ---
  const startQuiz = async () => {
    if (!selectedTopic) return;

    // Показываем заглушку пока грузится
    setLessonHtml('<h3>🤖 Генерируем тест...</h3><p>Пожалуйста, подождите 5-10 секунд.</p>');

    try {
      const res = await fetch(`/api/lessons/by-topic/${selectedTopic.id}/quiz`);
      const data = await res.json();

      // Парсим JSON строку, которая пришла с бэкенда
      // Backend возвращает { content: "[ {...}, {...} ]" }
      const parsedQuestions: QuizQuestion[] = JSON.parse(data.content);

      if (!Array.isArray(parsedQuestions) || parsedQuestions.length === 0) {
        throw new Error("Пустой тест");
      }

      setQuizQuestions(parsedQuestions);
      setCurrentQuestionIndex(0);
      setScore(0);
      setShowResult(false);
      setAnswerState('idle');
      setView('quiz'); // Переключаем экран

    } catch (e) {
      console.error(e);
      alert('Не удалось создать тест. Попробуйте еще раз.');
      // Возвращаем текст урока обратно, если ошибка
      handleTopicClick(selectedTopic);
    }
  };

  const handleAnswerClick = (index: number) => {
    if (answerState !== 'idle') return; // Блокируем клики, если уже ответили

    const currentQ = quizQuestions[currentQuestionIndex];
    const isCorrect = index === currentQ.correctIndex;

    setAnswerState(isCorrect ? 'correct' : 'wrong');
    if (isCorrect) setScore(s => s + 1);

    // Пауза перед следующим вопросом
    setTimeout(() => {
      if (currentQuestionIndex < quizQuestions.length - 1) {
        setCurrentQuestionIndex(prev => prev + 1);
        setAnswerState('idle');
      } else {
        setShowResult(true);
      }
    }, 2000); // 2 секунды чтобы прочитать объяснение
  };

  // --- РЕНДЕР ---

  if (view === 'login') return (
    <div className="card">
      <h1>Chall X Bot</h1>
      <button className="primary-btn" onClick={fetchTopics}>Начать 🚀</button>
    </div>
  );

  if (view === 'topics') return (
    <div className="container">
      <h2>Темы курса</h2>
      <div className="topics-grid">
        {topics.map(t => (
          <button key={t.id} className="topic-card" onClick={() => handleTopicClick(t)}>
            {t.name}
          </button>
        ))}
      </div>
    </div>
  );

  // Экран Урока
  if (view === 'lesson') return (
    <div className="container">
      {/* Хедер с навигацией */}
      <div className="lesson-header-actions" style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
        <button onClick={() => setView('topics')} className="back-btn" style={{ flex: 1 }}>
          ⬅ Назад
        </button>
        <button className="primary-btn" onClick={startQuiz} style={{ flex: 2, margin: 0 }}>
          🧠 Тест
        </button>
      </div>

      {/* Контент урока (HTML от AI) */}
      {/* Мы оборачиваем его в div с классом для стилизации */}
      <div className="ai-lesson-content" dangerouslySetInnerHTML={{ __html: lessonHtml }} />

      {/* Дублируем кнопку внизу для удобства */}
      <button className="primary-btn" style={{ marginTop: 30, width: '100%' }} onClick={startQuiz}>
        🚀 Начать тест
      </button>
    </div>
  );

  // --- ЭКРАН КВИЗА ---
  if (view === 'quiz') {
    if (showResult) {
      return (
        <div className="card quiz-result">
          <h1>🏁 Финиш!</h1>
          <div style={{ fontSize: '4rem', margin: '20px' }}>
            {score === 5 ? '🏆' : score >= 3 ? '😎' : '😐'}
          </div>
          <p style={{ fontSize: '1.5rem' }}>
            Ваш результат: <b>{score} / {quizQuestions.length}</b>
          </p>
          <button className="primary-btn" onClick={() => setView('topics')}>
            К списку тем
          </button>
        </div>
      );
    }

    const question = quizQuestions[currentQuestionIndex];
    return (
      <div className="container quiz-container">
        <div className="progress-bar-container">
          <div className="progress-fill" style={{ width: `${((currentQuestionIndex) / quizQuestions.length) * 100}%` }}></div>
        </div>
        <p className="step-text">Вопрос {currentQuestionIndex + 1} из {quizQuestions.length}</p>

        <h3 className="quiz-question">{question.question}</h3>

        <div className="options-list">
          {question.options.map((opt, idx) => {
            let btnClass = 'option-btn';
            // Логика подсветки
            if (answerState !== 'idle') {
              if (idx === question.correctIndex) btnClass += ' correct';
              else if (answerState === 'wrong' && idx === undefined) btnClass += ' wrong'; // (тут можно доработать логику подсветки нажатой кнопки)
            }

            // Если ответили неправильно, подсвечиваем красным именно нажатую (но тут у нас нет индекса нажатой в стейте, 
            // для простоты подсвечиваем правильный зеленым всегда, а остальные гасим)

            return (
              <button
                key={idx}
                className={btnClass}
                onClick={() => handleAnswerClick(idx)}
                style={answerState !== 'idle' && idx !== question.correctIndex ? { opacity: 0.5 } : {}}
              >
                {opt}
              </button>
            )
          })}
        </div>

        {/* Блок объяснения появляется после ответа */}
        {answerState !== 'idle' && (
          <div className={`explanation-box ${answerState}`}>
            <div style={{ fontSize: '2rem', marginBottom: 10 }}>
              {answerState === 'correct' ? '🎉' : '❌'}
            </div>
            <strong>{answerState === 'correct' ? 'Верно!' : 'Ошибка!'}</strong>
            <p>{question.explanation}</p>
          </div>
        )}
      </div>
    );
  }

  return null;
}

export default App;