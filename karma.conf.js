const path = require('path');

const webpackConfig = require('./webpack.config');
webpackConfig.mode = 'development';
delete webpackConfig.entry;
delete webpackConfig.output;

const jsPrefix = 'src/main/resources/frontend';
const specGlob = 'test/**/*.spec.js';
const specPath = path.join(__dirname, jsPrefix, specGlob);

module.exports = function (config) {
  config.set({
    frameworks: ['jasmine', 'webpack'],

    files: ['node_modules/jquery/dist/jquery.js', specPath],

    preprocessors: {
      '**/*.spec.js': ['webpack', 'sourcemap']
    },

    webpack: webpackConfig,

    reporters: ['spec'],

    browsers: ['ChromeHeadless'],

    customLaunchers: {
      Chrome_Headless: {
        base: 'Chrome',
        flags: [
          '--headless',
          '--disable-gpu',
          '--no-sandbox',
          '--remote-debugging-port=9222',
          '--window-size=1440,900'
        ]
      }
    },

    // used to produce test output for Jenkins
    junitReporter: {
      outputDir: 'target/karma'
    }
  });
};
