import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { Wallet, Moon, Sun } from 'lucide-react';

export const FloatingNavBar = ({ theme, toggleTheme }) => {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <motion.header
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
      style={{
        position: 'fixed',
        top: 0, left: 0, right: 0,
        padding: '24px',
        zIndex: 100,
        display: 'flex',
        justifyContent: 'center'
      }}
    >
      <motion.nav
        className="glass-panel"
        animate={{
          padding: scrolled ? '12px 24px' : '16px 32px',
          width: scrolled ? '800px' : '1200px',
        }}
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          maxWidth: '100%',
          borderRadius: '999px',
          background: 'var(--nav-bg)'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontWeight: 'bold', fontSize: '1.25rem' }} className="brand-font">
          <Wallet color="var(--accent-yellow-dark)" />
          <span>Full Wallet</span>
        </div>
        
        <div style={{ display: 'flex', gap: '32px', alignItems: 'center' }}>
          <a href="#features" style={{ color: 'var(--text-secondary)', textDecoration: 'none', fontWeight: 500 }}>Features</a>
          <a href="#setup" style={{ color: 'var(--text-secondary)', textDecoration: 'none', fontWeight: 500 }}>Setup</a>
          
          <button 
            onClick={toggleTheme}
            style={{
              background: 'transparent', border: 'none', 
              color: 'var(--text-primary)', cursor: 'pointer',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              width: '40px', height: '40px', borderRadius: '50%',
              backgroundColor: 'var(--card-bg)'
            }}
          >
            {theme === 'light' ? <Moon size={20} /> : <Sun size={20} />}
          </button>
        </div>
      </motion.nav>
    </motion.header>
  );
};
