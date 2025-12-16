export const Loader = ({ text = 'Загрузка...' }: { text?: string }) => (
    <div className="card" style={{ padding: '40px' }}>
        <div className="loader"></div>
        <p>{text}</p>
    </div>
);