import {useEffect, useRef} from "react";

const SSEManager = ({ onEvent, onError }) => {
  const eventSourceRef = useRef(null);

  useEffect(() => {
    // Create the SSE connection
    eventSourceRef.current = new EventSource('https://${process.env.BACKEND_API_HOST}/push-endpoint');

    // Event listener for SSE messages
    eventSourceRef.current.onmessage = (event) => {
      onEvent(event.data);
    };

    // Error listener for SSE connection errors
    eventSourceRef.current.onerror = (error) => {
      onError(error);
    };

    // Clean up the SSE connection on component unmount
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
      }
    };
  }, [onEvent, onError]);

  return null;
};

export default SSEManager;