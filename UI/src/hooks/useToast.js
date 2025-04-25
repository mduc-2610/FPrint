import { useState } from 'react';

export function useToast() {
  const [toast, setToast] = useState(null);
  const [isVisible, setIsVisible] = useState(false);

  const showToast = (options) => {
    setToast(options);
    setIsVisible(true);

    // Automatically hide toast after specified duration (default 3 seconds)
    const timer = setTimeout(() => {
      setIsVisible(false);
    }, options.duration || 3000);

    return () => clearTimeout(timer);
  };

  const hideToast = () => {
    setIsVisible(false);
  };

  return {
    toast: showToast,
    currentToast: toast,
    isVisible,
    hideToast
  };
}