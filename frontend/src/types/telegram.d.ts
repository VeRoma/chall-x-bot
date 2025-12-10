export { };

declare global {
    interface Window {
        Telegram: {
            WebApp: {
                initData: string;
                initDataUnsafe: {
                    query_id?: string;
                    user?: {
                        id: number;
                        first_name: string;
                        last_name?: string;
                        username?: string;
                        language_code?: string;
                    };
                    auth_date: string;
                    hash: string;
                };
                // Вот методы, которых не хватало:
                ready: () => void;
                expand: () => void;  // <--- МЫ ДОБАВИЛИ ЭТО
                close: () => void;

                // Полезные поля для стилей (на будущее)
                colorScheme: 'light' | 'dark';
                themeParams: {
                    bg_color?: string;
                    text_color?: string;
                    hint_color?: string;
                    button_color?: string;
                    button_text_color?: string;
                };
            };
        };
    }
}