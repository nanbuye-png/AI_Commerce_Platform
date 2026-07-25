import React, { useState, useCallback, useRef } from 'react';
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
  const navigate = useNavigate();
  const inputRef = useRef<HTMLInputElement>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const handleSearch = useCallback((keyword: string) => {
    const trimmed = keyword.trim();
    if (!trimmed) return;
    if (onSearch) {
      onSearch(trimmed);
    } else {
      navigate(`/search?q=${encodeURIComponent(trimmed)}`);
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
          border: '1px solid var(--color-border)',
          background: 'var(--color-bg-secondary)',
          padding: '0 16px',
          transition: 'border-color var(--transition-fast), box-shadow var(--transition-fast)',
        }}
        onFocus={() => {
          const container = inputRef.current?.parentElement?.parentElement;
          if (container) {
            container.style.borderColor = 'var(--color-accent)';
            container.style.boxShadow = '0 0 0 2px var(--color-accent-light)';
          }
        }}
        onBlur={() => {
          const container = inputRef.current?.parentElement?.parentElement;
          if (container) {
            container.style.borderColor = 'var(--color-border)';
            container.style.boxShadow = 'none';
          }
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