import React from 'react';
import { motion } from 'framer-motion';

export const GlassCard = ({ title, description, icon: Icon, delay = 0 }) => {
  return (
    <motion.div
      className="glass-panel"
      initial={{ opacity: 0, y: 30 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay, type: 'spring', stiffness: 100 }}
      style={{ padding: '32px', display: 'flex', flexDirection: 'column', gap: '16px', height: '100%' }}
    >
      {Icon && (
        <div style={{ 
          width: '56px', height: '56px', 
          borderRadius: '16px', 
          background: 'var(--card-bg)',
          border: '1px solid var(--card-border)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: 'var(--text-primary)'
        }}>
          <Icon size={28} />
        </div>
      )}
      <h3 className="text-h3 brand-font">{title}</h3>
      <p className="text-body">{description}</p>
    </motion.div>
  );
};
