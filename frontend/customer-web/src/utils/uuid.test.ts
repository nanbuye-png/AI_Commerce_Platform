import { describe, expect, it, vi } from 'vitest';
import { createUuid } from './uuid';

describe('createUuid', () => {
  it('creates a v4 UUID when randomUUID is unavailable on an HTTP origin', () => {
    vi.stubGlobal('crypto', {
      getRandomValues(bytes: Uint8Array) {
        for (let index = 0; index < bytes.length; index += 1) bytes[index] = index;
        return bytes;
      },
    });

    try {
      expect(createUuid()).toBe('00010203-0405-4607-8809-0a0b0c0d0e0f');
    } finally {
      vi.unstubAllGlobals();
    }
  });
});
