import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Download, ChartPie, CreditCard, Lock, Moon, Sun, Smartphone, CheckCircle } from 'lucide-react';
import { FloatingNavBar } from './components/FloatingNavBar';
import { GlassCard } from './components/GlassCard';
import { PillButton } from './components/PillButton';

function App() {
  const [theme, setTheme] = useState('light');
  const [downloading, setDownloading] = useState(false);

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(prev => prev === 'light' ? 'dark' : 'light');
  };

  const handleDownloadClick = () => {
    setDownloading(true);
    setTimeout(() => {
      document.getElementById('setup-guide').scrollIntoView({ behavior: 'smooth' });
      setDownloading(false);
    }, 2000);
  };

  const features = [
    { title: 'Smart Analytics', description: 'Gain insights into your spending habits with detailed charts and visual financial reports.', icon: ChartPie },
    { title: 'Expense Tracking', description: 'Log all your transactions on the go. Categorize your spending for better budgeting.', icon: CreditCard },
    { title: 'Dark Mode', description: 'Beautiful dark theme that is easy on the eyes, perfect for checking finances at night.', icon: Moon },
    { title: 'Secure Auth', description: 'Your financial data is protected with industry-standard security and encryption.', icon: Lock },
  ];

  const setupSteps = [
    { num: 1, title: 'Download the APK', desc: 'Click the download button above to get the latest Full Wallet.apk file.' },
    { num: 2, title: 'Open the File', desc: 'Locate the downloaded file in your device\'s notification panel and tap on it.' },
    { num: 3, title: 'Allow Installation', desc: 'If prompted, go to settings and enable "Install unknown apps".' },
    { num: 4, title: 'Install & Launch', desc: 'Tap "Install". Once the process is complete, open the app!' }
  ];

  return (
    <>
      <div className="bg-mesh"></div>
      
      <FloatingNavBar theme={theme} toggleTheme={toggleTheme} />
      
      <main className="container" style={{ paddingTop: '160px', paddingBottom: '100px' }}>
        {/* Hero Section */}
        <section style={{ 
          display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'space-between', 
          minHeight: '75vh', gap: '48px', marginBottom: '80px' 
        }}>
          <motion.div 
            style={{ flex: '1 1 500px' }}
            initial={{ opacity: 0, x: -50 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, type: 'spring' }}
          >
            <h1 className="text-hero" style={{ marginBottom: '24px' }}>
              Manage Your Finances <br/><span className="text-gradient">Effortlessly</span>
            </h1>
            <p className="text-body" style={{ marginBottom: '40px', maxWidth: '480px', fontSize: '1.25rem' }}>
              A modern, secure and user-friendly digital wallet application. Track expenses, monitor income, and achieve your financial goals with ease.
            </p>
            <div style={{ display: 'flex', gap: '20px', alignItems: 'center', flexWrap: 'wrap' }}>
              <PillButton 
                variant="primary" 
                icon={Download} 
                href="../Application/Full Wallet.apk" 
                download="Full Wallet.apk"
                onClick={handleDownloadClick}
              >
                Download APK
              </PillButton>
              <PillButton variant="secondary" href="#features">
                Explore Features
              </PillButton>
            </div>
            
            <AnimatePresence>
              {downloading && (
                <motion.div 
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0, y: -10 }}
                  style={{ 
                    marginTop: '24px', padding: '16px 24px', 
                    background: 'rgba(134, 239, 172, 0.15)', 
                    border: '1px solid rgba(134, 239, 172, 0.3)',
                    borderRadius: '16px', color: '#10b981',
                    display: 'flex', alignItems: 'center', gap: '12px',
                    fontWeight: 500
                  }}
                >
                  <CheckCircle size={20} />
                  <span>Downloading Full Wallet... see setup instructions below.</span>
                </motion.div>
              )}
            </AnimatePresence>
          </motion.div>
          
          <motion.div 
            style={{ flex: '1 1 400px', display: 'flex', justifyContent: 'center' }}
            initial={{ opacity: 0, scale: 0.9 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
          >
            <div className="glass-panel" style={{ 
              width: '100%', maxWidth: '380px', height: '650px', 
              padding: '24px', display: 'flex', flexDirection: 'column',
              boxShadow: '0 24px 60px rgba(0,0,0,0.1)'
            }}>
              <div style={{ display: 'flex', gap: '8px', justifyContent: 'center', marginBottom: '32px' }}>
                <div style={{ width: 40, height: 6, borderRadius: 3, background: 'var(--text-secondary)', opacity: 0.3 }} />
              </div>
              
              <div style={{ 
                background: 'var(--btn-gradient)', padding: '32px', 
                borderRadius: '20px', color: 'var(--btn-text)', marginBottom: '32px' 
              }}>
                <p style={{ opacity: 0.8, fontSize: '0.9rem', marginBottom: '8px' }}>Total Balance</p>
                <h2 style={{ fontSize: '2.5rem', fontWeight: 600 }}>$12,450.00</h2>
              </div>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <h4 style={{ fontWeight: 600, color: 'var(--text-secondary)' }}>Recent Transactions</h4>
                {[
                  { title: 'Groceries', amount: '-$120.00', color: '#ef4444' },
                  { title: 'Salary', amount: '+$4,200.00', color: '#10b981' },
                  { title: 'Utilities', amount: '-$85.00', color: '#ef4444' }
                ].map((tx, i) => (
                  <div key={i} style={{ 
                    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                    padding: '16px', borderRadius: '16px', background: 'var(--card-bg)',
                    border: '1px solid var(--card-border)'
                  }}>
                    <span style={{ fontWeight: 500 }}>{tx.title}</span>
                    <span style={{ color: tx.color, fontWeight: 600 }}>{tx.amount}</span>
                  </div>
                ))}
              </div>
            </div>
          </motion.div>
        </section>

        {/* Features Section */}
        <section id="features" className="section-padding">
          <h2 className="text-h2" style={{ textAlign: 'center', marginBottom: '64px' }}>Everything You Need</h2>
          <div style={{ 
            display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '32px' 
          }}>
            {features.map((feature, idx) => (
              <GlassCard key={idx} {...feature} delay={idx * 0.1} />
            ))}
          </div>
        </section>

        {/* Setup Guide Section */}
        <section id="setup-guide" className="section-padding" style={{ maxWidth: '800px', margin: '0 auto' }}>
          <h2 className="text-h2" style={{ textAlign: 'center', marginBottom: '64px' }}>Installation Guide</h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
            {setupSteps.map((step, idx) => (
              <motion.div 
                key={idx}
                className="glass-panel"
                initial={{ opacity: 0, x: -30 }}
                whileInView={{ opacity: 1, x: 0 }}
                viewport={{ once: true }}
                transition={{ delay: idx * 0.1, type: 'spring' }}
                style={{ padding: '32px', display: 'flex', gap: '24px', alignItems: 'flex-start' }}
              >
                <div style={{ 
                  width: '48px', height: '48px', borderRadius: '50%', 
                  background: 'var(--btn-gradient)', color: 'var(--btn-text)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '1.25rem', fontWeight: 'bold', flexShrink: 0
                }}>
                  {step.num}
                </div>
                <div>
                  <h3 className="text-h3 brand-font" style={{ marginBottom: '8px' }}>{step.title}</h3>
                  <p className="text-body">{step.desc}</p>
                </div>
              </motion.div>
            ))}
          </div>
        </section>
      </main>

      <footer style={{ 
        textAlign: 'center', padding: '40px', 
        borderTop: '1px solid var(--card-border)', color: 'var(--text-secondary)'
      }}>
        <p>&copy; {new Date().getFullYear()} Full Wallet. All rights reserved.</p>
      </footer>
    </>
  );
}

export default App;
