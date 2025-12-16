export const ProgressBar = ({ current, total }: { current: number; total: number }) => (
    <div className="progress-bar-container">
        <div
            className="progress-fill"
            style={{ width: `${(current / total) * 100}%` }}
        />
    </div>
);