import React from 'react';

// chatgpt-style loading dots animation
function LoadingAnimation() {
    return (
        <div className="message assistant">
            <div className="message-content">
                <div className="loading-dots">
                    <span></span>
                    <span></span>
                    <span></span>
                </div>
            </div>
        </div>
    );
}

export default LoadingAnimation;
