import { useEffect, useState } from 'react';
import type { Topic, User } from '../types';
import { Loader } from '../components/Loader';

type Props = {
    user: User | null;
    onTopicClick: (topic: Topic) => void;
    onStartChallenge: () => void;
};

export const TopicsScreen = ({ user, onTopicClick, onStartChallenge }: Props) => {
    const [topics, setTopics] = useState<Topic[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetch('/api/topics')
            .then(res => res.json())
            .then(data => {
                setTopics(Array.isArray(data) ? data : []);
                setLoading(false);
            })
            .catch(() => setLoading(false));
    }, []);

    if (loading) return <Loader text="Загружаем темы..." />;

    return (
        <div className="container">
            <div className="header-row">
                <h3>Привет, {user?.firstName}!</h3>
                <span className="balance-badge">⭐ 0</span>
            </div>

            <button className="primary-btn word-challenge-btn" onClick={onStartChallenge}>
                📚 Учить слова (Daily Challenge)
            </button>

            <h4 style={{ marginTop: 20, marginBottom: 10, textAlign: 'left' }}>Грамматика:</h4>
            <div className="topics-grid">
                {topics.map(t => (
                    <button key={t.id} className="topic-card" onClick={() => onTopicClick(t)}>
                        {t.name}
                    </button>
                ))}
            </div>
        </div>
    );
};