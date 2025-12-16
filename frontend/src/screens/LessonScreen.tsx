import { useEffect, useState } from 'react';
import type { Topic, LessonData } from '../types';
import { Loader } from '../components/Loader';

type Props = {
    topic: Topic;
    onBack: () => void;
    onStartQuiz: () => void;
};

export const LessonScreen = ({ topic, onBack, onStartQuiz }: Props) => {
    const [lesson, setLesson] = useState<LessonData | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(false);

    useEffect(() => {
        fetch(`/api/lessons/by-topic/${topic.id}`)
            .then(res => {
                if (!res.ok) throw new Error();
                return res.json();
            })
            .then(data => {
                setLesson(data);
                setLoading(false);
            })
            .catch(() => {
                setError(true);
                setLoading(false);
            });
    }, [topic.id]);

    if (loading) return <Loader text="Загружаем урок..." />;
    if (error || !lesson) return (
        <div className="card">
            <h3>Ошибка загрузки урока</h3>
            <button onClick={onBack} className="primary-btn">Назад</button>
        </div>
    );

    return (
        <div className="container">
            <div className="lesson-header-actions" style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
                <button onClick={onBack} className="back-btn" style={{ flex: 1 }}>⬅ Меню</button>
                <button className="primary-btn" onClick={onStartQuiz} style={{ flex: 2, margin: 0 }}>🧠 Тест</button>
            </div>
            <div className="ai-lesson-content">
                <h2>{lesson.title}</h2>
                <br />
                <div dangerouslySetInnerHTML={{ __html: lesson.content }} />
            </div>
            <button className="primary-btn" style={{ marginTop: 30, width: '100%' }} onClick={onStartQuiz}>
                🚀 Начать тест
            </button>
        </div>
    );
};