// eslint-disable @typescript-eslint/no-require-imports
const { defineConfig } = require('eslint/config');

const globals = require('globals');
const vue = require('eslint-plugin-vue');
const tseslint = require('typescript-eslint');
const js = require('@eslint/js');

const tsPlugin = tseslint.plugin;

module.exports = defineConfig([
  js.configs.recommended,
  tseslint.configs.recommended,
  ...vue.configs['flat/essential'],
  {
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser
      },

      globals: {
        ...globals.browser,
        $: true,
        bootstrap: true,
        Map: true,
        Set: true,
        Promise: true
      }
    },

    plugins: {
      tsPlugin,
      vue
    },

    rules: {
      '@typescript-eslint/no-require-imports': 'off',

      eqeqeq: ['error', 'always'],

      'no-console': [
        'error',
        {
          allow: ['warn', 'error']
        }
      ],

      'no-unused-vars': [
        'error',
        {
          vars: 'all',
          args: 'none'
        }
      ],

      'no-var': ['error'],

      'vue/multi-word-component-names': [
        'error',
        {
          ignores: ['Logo']
        }
      ]
    }
  },
  {
    files: [
      '**/eslint.config.js',
      '**/.prettierrc.js',
      '**/karma.conf.js',
      '**/webpack.config.js'
    ],

    languageOptions: {
      globals: {
        ...Object.fromEntries(
          Object.entries(globals.browser).map(([key]) => [key, 'off'])
        ),
        ...globals.node
      }
    }
  },
  {
    files: ['src/main/resources/frontend/test/**/*.js'],

    languageOptions: {
      globals: {
        ...globals.jasmine
      }
    }
  }
]);
