import React, { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface SearchBarProps {
  placeholder?: string;
  autoFocus?: boolean;
  onSearch?: (keyword: string) => void;
  size?: 'md' | 'lg';
}

const SearchBar: React.FC<SearchBarProps> = ({
  placeholder = '搜索商品...',
  autoFocus = false,
  onSearch,
  size = 'md',
}) => {
  const [value, setValue] = useState('');
  const [focused, setFocused] = useState(false);
  const navigate = useNavigate();
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => () => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
  }, []);

  const handleSearch = useCallback((keyword: string) => {
    const trimmed = keyword.trim();
    if (!trimmed) return;
    if (onSearch) {
      onSearch(trimmed);
    } else {
      void navigate(`/search?q=${encodeURIComponent(trimmed)}`);
    }
  }, [navigate, onSearch]);

  const handleChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setValue(val);

    // Debounced search suggestion trigger
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (val.trim().length >= 2) {
      debounceRef.current = setTimeout(() => {
        // Future: trigger search suggestions
      }, 300);
    }
  }, []);

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch(value);
    }
  }, [handleSearch, value]);

  const handleClear = useCallback(() => {
    setValue('');
    inputRef.current?.focus();
  }, []);

  const height = size === 'lg' ? 48 : 40;

  return (
    <div style={{ position: 'relative', width: '100%' }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          height,
          borderRadius: 'var(--radius-full)',
          background: 'var(--color-bg-secondary)',
          border: `1px solid ${focused ? 'var(--color-accent)' : 'var(--color-border)'}`,
          boxShadow: focused ? '0 0 0 2px var(--color-accent-light)' : 'none',
          transition: 'border-color var(--transition-fast), box-shadow var(--transition-fast)',
        }}
      >
        <span style={{ color: 'var(--color-text-tertiary)', marginRight: 8, fontSize: '16px' }}>
          🔍
        </span>
        <input
          ref={inputRef}
          value={value}
          onChange={handleChange}
          onKeyDown={handleKeyDown}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          placeholder={placeholder}
          autoFocus={autoFocus}
          style={{
            flex: 1,
            border: 'none',
            outline: 'none',
            background: 'transparent',
            fontSize: size === 'lg' ? '16px' : '14px',
            color: 'var(--color-text-primary)',
            height: '100%',
          }}
        />
        {value && (
          <button
            onClick={handleClear}
            style={{
              width: 24,
              height: 24,
              borderRadius: '50%',
              background: 'var(--color-border)',
              border: 'none',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '12px',
              color: '#fff',
              marginLeft: 4,
            }}
          >
            ✕
          </button>
        )}
        <button
          onClick={() => handleSearch(value)}
          style={{
            marginLeft: 8,
            padding: '6px 16px',
            borderRadius: 'var(--radius-full)',
            background: 'var(--color-accent)',
            color: '#fff',
            fontSize: '13px',
            fontWeight: 500,
            border: 'none',
            cursor: 'pointer',
            whiteSpace: 'nowrap',
          }}
        >
          搜索
        </button>
      </div>
    </div>
  );
};

export default SearchBar;