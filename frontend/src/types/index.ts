export type User = {
    id: number;
    username: string;
    firstName: string;
};

export type Topic = {
    id: number;
    name: string;
    isActive?: boolean;
};

export type LessonData = {
    id: number;
    title: string;
    content: string;
};

// Типы для вопросов
export type GrammarQuestion = {
    question: string;
    options: string[];
    correctIndex: number;
    explanation: string;
};

export type WordQuestion = {
    word: string;
    options: string[];
    correctIndex: number;
    translationFull: string;
};

// Универсальный тип, чтобы QuizScreen понимал и то, и другое
export type UnifiedQuestion = {
    type: 'grammar' | 'word';
    mainText: string;       // question или word
    options: string[];
    correctIndex: number;
    explanation: string;    // explanation или translationFull
};