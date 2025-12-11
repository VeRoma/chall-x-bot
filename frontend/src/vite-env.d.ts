/// <reference types="vite/client" />

interface Window {
    Telegram?: {
        WebApp: {
            initData: string;
            initDataUnsafe: {
                query_id?: string;
                user?: WebAppUser;
                receiver?: WebAppUser;
                chat?: WebAppChat;
                start_param?: string;
                can_send_after?: number;
                auth_date: number;
                hash: string;
            };
            version: string;
            platform: string;
            colorScheme: 'light' | 'dark';
            themeParams: ThemeParams;
            isExpanded: boolean;
            viewportHeight: number;
            viewportStableHeight: number;
            headerColor: string;
            backgroundColor: string;
            isClosingConfirmationEnabled: boolean;

            // Кнопки
            BackButton: {
                isVisible: boolean;
                onClick(callback: () => void): void;
                offClick(callback: () => void): void;
                show(): void;
                hide(): void;
            };
            MainButton: {
                text: string;
                color: string;
                textColor: string;
                isVisible: boolean;
                isActive: boolean;
                isProgressVisible: boolean;
                setText(text: string): void;
                onClick(callback: () => void): void;
                offClick(callback: () => void): void;
                show(): void;
                hide(): void;
                enable(): void;
                disable(): void;
                showProgress(leaveActive?: boolean): void;
                hideProgress(): void;
                setParams(params: {
                    text?: string;
                    color?: string;
                    text_color?: string;
                    is_active?: boolean;
                    is_visible?: boolean;
                }): void;
            };
            HapticFeedback: any;

            // Методы управления
            ready(): void;
            expand(): void;
            close(): void;

            // 🔥 ВАЖНО: Этих методов не хватало
            setHeaderColor?(color: string): void;
            setBackgroundColor?(color: string): void;

            // События
            onEvent(eventType: string, eventHandler: () => void): void;
            offEvent(eventType: string, eventHandler: () => void): void;
            sendData(data: any): void;
        };
    };
}

// Типы данных
interface WebAppUser {
    id: number;
    is_bot?: boolean;
    first_name: string;
    last_name?: string;
    username?: string;
    language_code?: string;
    is_premium?: boolean;
    photo_url?: string;
}

interface WebAppChat {
    id: number;
    type: 'group' | 'supergroup' | 'channel';
    title: string;
    username?: string;
    photo_url?: string;
}

interface ThemeParams {
    bg_color?: string;
    text_color?: string;
    hint_color?: string;
    link_color?: string;
    button_color?: string;
    button_text_color?: string;
    secondary_bg_color?: string;
}