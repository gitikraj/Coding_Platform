"use client";

import * as React from "react";

type ToastVariant = "default" | "destructive";

export type Toast = {
  id: string;
  title?: React.ReactNode;
  description?: React.ReactNode;
  action?: React.ReactNode;
  variant?: ToastVariant;
  duration?: number;
};

type ToastState = {
  toasts: Toast[];
};

type ToastListeners = (toastState: ToastState) => void;

const listeners = new Set<ToastListeners>();
let memoryState: ToastState = { toasts: [] };
const TOAST_LIMIT = 5;

const DEFAULT_DURATION = 4000;

function dispatch(toast: Toast | null) {
  if (toast) {
    memoryState = {
      toasts: [...memoryState.toasts, toast],
    };
    if (memoryState.toasts.length > TOAST_LIMIT) {
      memoryState = {
        toasts: memoryState.toasts.slice(memoryState.toasts.length - TOAST_LIMIT),
      };
    }
  } else {
    memoryState = {
      toasts: [],
    };
  }
  listeners.forEach((listener) => listener(memoryState));
}

function createToast(toast: Omit<Toast, "id">) {
  const id = typeof crypto !== "undefined" ? crypto.randomUUID() : Math.random().toString(36).slice(2);
  const nextToast: Toast = {
    id,
    duration: toast.duration ?? DEFAULT_DURATION,
    ...toast,
  };
  dispatch(nextToast);

  if (nextToast.duration && nextToast.duration > 0) {
    setTimeout(() => dismiss(id), nextToast.duration);
  }

  return {
    id,
    dismiss: () => dismiss(id),
  };
}

export function toast(toast: Omit<Toast, "id">) {
  return createToast(toast);
}

export function dismiss(toastId?: string) {
  if (toastId) {
    memoryState = {
      toasts: memoryState.toasts.filter((toast) => toast.id !== toastId),
    };
  } else {
    memoryState = { toasts: [] };
  }
  listeners.forEach((listener) => listener(memoryState));
}

export function useToast() {
  const [state, setState] = React.useState<ToastState>(memoryState);

  React.useEffect(() => {
    const listener: ToastListeners = (nextState) => {
      setState(nextState);
    };
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  }, []);

  return {
    ...state,
    toast,
    dismiss,
  };
}

