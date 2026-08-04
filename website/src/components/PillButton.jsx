import React from 'react';
import { motion } from 'framer-motion';

export const PillButton = ({ children, variant = 'primary', onClick, href, download, icon: Icon, ...rest }) => {
  const isPrimary = variant === 'primary';
  
  const baseStyles = {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    padding: '16px 32px',
    borderRadius: '999px',
    fontWeight: 600,
    fontSize: '1.125rem',
    cursor: 'pointer',
    border: 'none',
    textDecoration: 'none',
    position: 'relative',
    overflow: 'hidden',
  };

  const primaryStyles = {
    ...baseStyles,
    background: 'var(--btn-gradient)',
    color: 'var(--btn-text)',
    boxShadow: '0 8px 24px rgba(0, 0, 0, 0.1)',
  };

  const secondaryStyles = {
    ...baseStyles,
    background: 'var(--card-bg)',
    backdropFilter: 'blur(10px)',
    border: '1px solid var(--card-border)',
    color: 'var(--text-primary)',
  };

  const combinedStyles = isPrimary ? primaryStyles : secondaryStyles;

  const hoverAnimation = {
    scale: 1.05,
    boxShadow: isPrimary ? '0 12px 32px rgba(0,0,0,0.2)' : '0 12px 32px rgba(0,0,0,0.1)',
  };

  const tapAnimation = {
    scale: 0.95,
  };

  const MotionComponent = href ? motion.a : motion.button;
  const props = href ? { href, download, ...rest } : { onClick, ...rest };

  return (
    <MotionComponent
      style={combinedStyles}
      whileHover={hoverAnimation}
      whileTap={tapAnimation}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: 'spring', stiffness: 400, damping: 25 }}
      {...props}
    >
      {Icon && <Icon size={20} />}
      {children}
    </MotionComponent>
  );
};
