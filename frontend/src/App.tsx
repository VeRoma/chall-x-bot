import { useEffect, useState } from 'react';
import './App.css';

// --- ТИПЫ ДАННЫХ ---
type Topic = { id: number; name: string; isActive?: boolean; active?: boolean; };
type LessonData = { id: number; title: string; content: string; };
type QuizQuestion = {
  question: string;
  options: string[];
  correctIndex: number;
  explanation: string;
};

// НОВЫЙ ТИП: Вопрос по словам
type WordQuizQuestion = {
  word: string;
  options: string[];
  correctIndex: number;
  translationFull: string;
};

type ViewState = 'login' | 'topics' | 'lesson' | 'quiz' | 'word-quiz';

function App() {
  const [user, setUser] = useState<{ firstName: string; id: number; username: string } | null>(null);
  const [status, setStatus] = useState<string>('Загрузка...');
  const [view, setView] = useState<ViewState>('login');

  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);
  const [lessonHtml, setLessonHtml] = useState<string>('');

  // Состояние Квиза (Грамматика)
  const [quizQuestions, setQuizQuestions] = useState<QuizQuestion[]>([]);

  // Состояние Квиза (СЛОВА)
  const [wordQuestions, setWordQuestions] = useState<WordQuizQuestion[]>([]);

  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [score, setScore] = useState(0);
  const [showResult, setShowResult] = useState(false);
  const [answerState, setAnswerState] = useState<'idle' | 'correct' | 'wrong'>('idle');

  // --- ИНИЦИАЛИЗАЦИЯ ---
  useEffect(() => {
    const tg = window.Telegram?.WebApp;
    if (tg) {
      tg.ready();
      try { tg.expand?.(); } catch { }

      const applyTheme = () => {
        document.documentElement.setAttribute('data-theme', tg.colorScheme);
        if (tg.themeParams && tg.themeParams.bg_color) {
          tg.setHeaderColor?.(tg.themeParams.bg_color);
          tg.setBackgroundColor?.(tg.themeParams.bg_color);
        }
      };
      applyTheme();
      tg.onEvent('themeChanged', applyTheme);

      const u = tg.initDataUnsafe?.user;
      if (u) setUser({ id: u.id, username: u.username || '', firstName: u.first_name });
    } else {
      setUser({ id: 123, username: 'dev', firstName: 'Developer' });
    }
  }, []);

  const fetchTopics = async () => {
    try {
      const res = await fetch('/api/topics');
      const data = await res.json();
      setTopics(data.length ? data : []);
      setView('topics');
    } catch { setStatus('Ошибка загрузки тем'); }
  };

  // --- ЗАПУСК УЧИТЬ СЛОВА ---
  const startWordQuiz = async () => {
    setStatus('Загружаем слова...');
    try {
      const res = await fetch('/api/vocabulary/challenge');
      if (!res.ok) throw new Error('Ошибка API');

      const data: WordQuizQuestion[] = await res.json();
      if (data.length === 0) {
        alert('Словарь пока пуст! Подождите генерацию на сервере.');
        return;
      }

      setWordQuestions(data);
      setCurrentQuestionIndex(0);
      setScore(0);
      setShowResult(false);
      setAnswerState('idle');
      setView('word-quiz');
    } catch (e) {
      console.error(e);
      alert('Не удалось загрузить слова.');
    }
  };

  const handleTopicClick = async (topic: Topic) => {
    setSelectedTopic(topic);
    setStatus(`Грузим урок...`);
    try {
      const res = await fetch(`/api/lessons/by-topic/${topic.id}`);
      if (res.ok) {
        const data: LessonData = await res.json();
        setLessonHtml(`<h2>${data.title}</h2><br/>${data.content}`);
        setView('lesson');
      }
    } catch {
      setLessonHtml('<h3>Ошибка загрузки урока</h3>');
      setView('lesson');
    }
  };

  const startQuiz = async () => {
    if (!selectedTopic) return;
    setLessonHtml('<h3>🤖 Генерируем тест...</h3>');
    try {
      const res = await fetch(`/api/lessons/by-topic/${selectedTopic.id}/quiz`);
      const data = await res.json();
      const parsedQuestions: QuizQuestion[] = JSON.parse(data.content);
      if (!Array.isArray(parsedQuestions)) throw new Error("Пустой тест");

      setQuizQuestions(parsedQuestions);
      setCurrentQuestionIndex(0);
      setScore(0);
      setShowResult(false);
      setAnswerState('idle');
      setView('quiz');
    } catch (e) {
      alert('Ошибка запуска теста.');
      handleTopicClick(selectedTopic);
    }
  };

  // Единый обработчик ответов (для обоих типов квизов)
  const handleAnswerClick = (index: number, type: 'grammar' | 'word') => {
    if (answerState !== 'idle') return;

    let isCorrect = false;
    let totalQuestions = 0;

    if (type === 'grammar') {
      const currentQ = quizQuestions[currentQuestionIndex];
      isCorrect = index === currentQ.correctIndex;
      totalQuestions = quizQuestions.length;
    } else {
      const currentQ = wordQuestions[currentQuestionIndex];
      isCorrect = index === currentQ.correctIndex;
      totalQuestions = wordQuestions.length;
    }

    setAnswerState(isCorrect ? 'correct' : 'wrong');
    if (isCorrect) setScore(s => s + 1);

    // Для слов даем больше времени почитать "Полный перевод" (3.5 сек), для грамматики 2 сек
    const delay = type === 'word' ? 3500 : 2000;

    setTimeout(() => {
      if (currentQuestionIndex < totalQuestions - 1) {
        setCurrentQuestionIndex(prev => prev + 1);
        setAnswerState('idle');
      } else {
        setShowResult(true);
      }
    }, delay);
  };

  // --- РЕНДЕР ---
  if (view === 'login') return (
    <div className="card">
      <h1>Chall_X_Bot</h1>
      <button className="primary-btn" onClick={fetchTopics}>Грамматика 🇬🇧</button>
      <div style={{ height: 20 }}></div>
      <button className="primary-btn" style={{ backgroundColor: '#e91e63' }} onClick={startWordQuiz}>
        📚 Слова (Топ 100)
      </button>
    </div>
  );

  if (view === 'topics') return (
    <div className="container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2>Темы курса</h2>
        {/* Кнопка домой */}
        <button className="back-btn" style={{ padding: '5px 10px' }} onClick={() => setView('login')}>🏠</button>
      </div>
      <div className="topics-grid">
        {topics.map(t => (
          <button key={t.id} className="topic-card" onClick={() => handleTopicClick(t)}>
            {t.name}
          </button>
        ))}
      </div>
    </div>
  );

  if (view === 'lesson') return (
    <div className="container">
      <div className="lesson-header-actions" style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
        <button onClick={() => setView('topics')} className="back-btn" style={{ flex: 1 }}>⬅ Назад</button>
        <button className="primary-btn" onClick={startQuiz} style={{ flex: 2, margin: 0 }}>🧠 Тест</button>
      </div>
      <div className="ai-lesson-content" dangerouslySetInnerHTML={{ __html: lessonHtml }} />
      <button className="primary-btn" style={{ marginTop: 30, width: '100%' }} onClick={startQuiz}>🚀 Начать тест</button>
    </div>
  );

  // --- РЕНДЕР КВИЗА (СЛОВА) ---
  if (view === 'word-quiz') {
    if (showResult) {
      return (
        <div className="card quiz-result">
          <h1>🏁 Результат</h1>
          <p style={{ fontSize: '1.5rem' }}>{score} / {wordQuestions.length}</p>
          <button className="primary-btn" onClick={() => setView('login')}>В меню</button>
        </div>
      );
    }
    const q = wordQuestions[currentQuestionIndex];
    return (
      <div className="container quiz-container">
        <div className="progress-bar-container">
          <div className="progress-fill" style={{ width: `${((currentQuestionIndex) / wordQuestions.length) * 100}%` }}></div>
        </div>

        {/* Слово КРУПНО */}
        <h1 style={{ fontSize: '3rem', margin: '20px 0' }}>{q.word}</h1>

        <div className="options-list">
          {q.options.map((opt, idx) => {
            let btnClass = 'option-btn';
            if (answerState !== 'idle') {
              if (idx === q.correctIndex) btnClass += ' correct';
              else if (answerState === 'wrong' && idx === undefined) btnClass += ' wrong';
            }
            return (
              <button key={idx} className={btnClass} onClick={() => handleAnswerClick(idx, 'word')}>
                {opt}
              </button>
            )
          })}
        </div>

        {/* ПОЛНЫЙ ПЕРЕВОД (Шторка снизу) */}
        {answerState !== 'idle' && (
          <div className={`explanation-box ${answerState}`} style={{ textAlign: 'left' }}>
            <div style={{ textAlign: 'center', fontSize: '1.5rem', marginBottom: 10 }}>
              {answerState === 'correct' ? '🎉 Правильно!' : '🤔 Почти...'}
            </div>
            <hr style={{ opacity: 0.2 }} />
            <p style={{ whiteSpace: 'pre-wrap', lineHeight: '1.5' }}>
              {q.translationFull}
            </p>
          </div>
        )}
      </div>
    );
  }

  // --- РЕНДЕР КВИЗА (ГРАММАТИКА) ---
  if (view === 'quiz') {
    if (showResult) {
      return (
        <div className="card quiz-result">
          <h1>🏁 Финиш!</h1>
          <p style={{ fontSize: '1.5rem' }}>{score} / {quizQuestions.length}</p>
          <button className="primary-btn" onClick={() => setView('topics')}>К темам</button>
        </div>
      );
    }
    const q = quizQuestions[currentQuestionIndex];
    return (
      <div className="container quiz-container">
        <div className="progress-bar-container">
          <div className="progress-fill" style={{ width: `${((currentQuestionIndex) / quizQuestions.length) * 100}%` }}></div>
        </div>
        <p className="step-text">Вопрос {currentQuestionIndex + 1} / {quizQuestions.length}</p>
        <h3 className="quiz-question">{q.question}</h3>
        <div className="options-list">
          {q.options.map((opt, idx) => {
            let btnClass = 'option-btn';
            if (answerState !== 'idle') {
              if (idx === q.correctIndex) btnClass += ' correct';
              else if (answerState === 'wrong' && idx === undefined) btnClass += ' wrong';
            }
            return (
              <button key={idx} className={btnClass} onClick={() => handleAnswerClick(idx, 'grammar')}>
                {opt}
              </button>
            )
          })}
        </div>
        {answerState !== 'idle' && (
          <div className={`explanation-box ${answerState}`}>
            <strong>{answerState === 'correct' ? 'Верно!' : 'Ошибка!'}</strong>
            <p>{q.explanation}</p>
          </div>
        )}
      </div>
    );
  }
  return null;
}

export default App;