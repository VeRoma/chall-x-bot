import { useEffect, useState } from 'react';
import type { UnifiedQuestion, Topic, GrammarQuestion, WordQuestion } from '../types';
import { Loader } from '../components/Loader';
import { ProgressBar } from '../components/ProgressBar';

type Props = {
    mode: 'grammar' | 'word';
    topic?: Topic; // Только для грамматики
    onBack: () => void;
};

export const QuizScreen = ({ mode, topic, onBack }: Props) => {
    const [questions, setQuestions] = useState<UnifiedQuestion[]>([]);
    const [loading, setLoading] = useState(true);

    const [currentIndex, setCurrentIndex] = useState(0);
    const [score, setScore] = useState(0);
    const [showResult, setShowResult] = useState(false);
    const [answerState, setAnswerState] = useState<'idle' | 'correct' | 'wrong'>('idle');

    useEffect(() => {
        const fetchData = async () => {
            try {
                let data: UnifiedQuestion[] = [];

                if (mode === 'word') {
                    const res = await fetch('/api/vocabulary/challenge');
                    const raw: WordQuestion[] = await res.json();
                    if (raw.length === 0) throw new Error("Empty vocabulary");

                    data = raw.map(q => ({
                        type: 'word',
                        mainText: q.word,
                        options: q.options,
                        correctIndex: q.correctIndex,
                        explanation: q.translationFull
                    }));
                } else if (mode === 'grammar' && topic) {
                    const res = await fetch(`/api/lessons/by-topic/${topic.id}/quiz`);
                    const json = await res.json();
                    const raw: GrammarQuestion[] = JSON.parse(json.content);

                    data = raw.map(q => ({
                        type: 'grammar',
                        mainText: q.question,
                        options: q.options,
                        correctIndex: q.correctIndex,
                        explanation: q.explanation
                    }));
                }

                setQuestions(data);
                setLoading(false);
            } catch (e) {
                alert('Не удалось загрузить тест');
                onBack();
            }
        };
        fetchData();
    }, [mode, topic, onBack]);

    const handleAnswer = (idx: number) => {
        if (answerState !== 'idle') return;

        const q = questions[currentIndex];
        const isCorrect = idx === q.correctIndex;

        setAnswerState(isCorrect ? 'correct' : 'wrong');
        if (isCorrect) setScore(s => s + 1);

        // Слова держим подольше, чтобы прочитать пример (3.5 сек), грамматику быстрее (2 сек)
        const delay = mode === 'word' ? 3500 : 2000;

        setTimeout(() => {
            if (currentIndex < questions.length - 1) {
                setCurrentIndex(prev => prev + 1);
                setAnswerState('idle');
            } else {
                setShowResult(true);
            }
        }, delay);
    };

    if (loading) return <Loader text="Генерируем тест..." />;

    if (showResult) {
        return (
            <div className="card quiz-result">
                <h1>🏁 Результат</h1>
                <p className="score-text">{score} / {questions.length}</p>
                <button className="primary-btn" onClick={onBack}>В меню</button>
            </div>
        );
    }

    const q = questions[currentIndex];

    return (
        <div className="container quiz-container">
            <ProgressBar current={currentIndex} total={questions.length} />

            {/* Заголовок: Для слов большой, для вопросов поменьше */}
            {mode === 'word'
                ? <h1 style={{ fontSize: '3rem', margin: '20px 0' }}>{q.mainText}</h1>
                : <h3 className="quiz-question">{q.mainText}</h3>
            }

            <div className="options-list">
                {q.options.map((opt, idx) => {
                    let btnClass = 'option-btn';
                    if (answerState !== 'idle') {
                        if (idx === q.correctIndex) btnClass += ' correct';
                        else if (answerState === 'wrong' && idx === undefined) btnClass += ' wrong';
                    }
                    return (
                        <button key={idx} className={btnClass} onClick={() => handleAnswer(idx)}>
                            {opt}
                        </button>
                    )
                })}
            </div>

            {answerState !== 'idle' && (
                <div className={`explanation-box ${answerState}`} style={{ textAlign: 'left' }}>
                    <div style={{ textAlign: 'center', marginBottom: 5, fontSize: '1.2rem' }}>
                        {answerState === 'correct' ? '🎉 Верно!' : '🤔 Почти...'}
                    </div>
                    {mode === 'word' && <hr style={{ opacity: 0.2 }} />}
                    <p style={{ whiteSpace: 'pre-wrap' }}>{q.explanation}</p>
                </div>
            )}
        </div>
    );
};