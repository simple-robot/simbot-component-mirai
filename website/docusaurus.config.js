// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

const currentVersion = require('./static/version.json').version
const firstYear = 2022
const currentYear = new Date().getFullYear()
const copyrightYear = currentYear === firstYear ? firstYear : firstYear + '-' + currentYear

const keywords = ['forte', 'simple robot', 'simbot', 'bot', 'qqbot', 'qq机器人', 'mirai', 'mirai组件']

/** @type {import('@docusaurus/types').Config} */
async function config() {
  return {
    title: 'Simple Robot Mirai组件库',
    tagline: `基于mirai框架的simbot组件实现库`,
    favicon: 'img/favicon.png',

    url: 'https://component-mirai.simbot.forte.love',
    baseUrl: '/',

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: 'Simple Robot', // Usually your GitHub org/username.
    projectName: 'simbot component mirai website', // Usually your repo name.

    onBrokenLinks: 'warn',
    onBrokenMarkdownLinks: 'warn',

    // Even if you don't use internalization, you can use this field to set useful
    // metadata like html lang. For example, if your site is Chinese, you may want
    // to replace "en" with "zh-Hans".
    i18n: {
      defaultLocale: 'zh-Hans',
      locales: ['zh-Hans'],
    },

    plugins: [
      // https://github.com/flexanalytics/plugin-image-zoom
      'plugin-image-zoom'
    ],

    presets: [
      [
        'classic',
        /** @type {import('@docusaurus/preset-classic').Options} */
        ({
          docs: {
            sidebarPath: require.resolve('./sidebars.js'),
            routeBasePath: 'docs',
            editUrl:
                'https://github.com/simple-robot/simbot-component-mirai/tree/main/website',
            breadcrumbs: true,
            showLastUpdateTime: true,
            lastVersion: 'current',
            versions: {
             current: {
               label: currentVersion,
               badge: true
               // path: currentVersion,
               // banner: 'BANNER',
             },
            },


          },
          blog: false,
          sitemap: {
            changefreq: 'weekly',
            priority: 0.5,
            ignorePatterns: ['/tags/**'],
            filename: 'sitemap.xml',
          },

          theme: {
            customCss: require.resolve('./src/css/custom.css'),
          },
        }),
      ],
    ],

    themes: [
      [
        // https://github.com/easyops-cn/docusaurus-search-local#installation
        require.resolve("@easyops-cn/docusaurus-search-local"),
        {
          hashed: true,
          language: ['zh'],
          explicitSearchResultPath: true
        }
      ]
    ],

    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
          metadata: [{
            name: 'keywords', content:  keywords.join(',')
          }],

          // Replace with your project's social card
          image: 'img/logo.png',
          docs: {
            sidebar: {
              hideable: true,
              autoCollapseCategories: true,

            }
          },
          // 公告
          // announcementBar: {
          //   id: 'announcementBar-still_under_construction',
          //   content: `文档尚不完全，但也够用 🍵 v3.0.0 发布在即，可前往 <a href="https://github.com/orgs/simple-robot/discussions" target="_blank"><b>社区</b></a> 交流或通过 <a href="https://github.com/simple-robot/simpler-robot/issues" target="_blank"><b>Issues</b></a> 反馈问题 😊😊😊‍`,
          //   // backgroundColor: '#FFB906',
          //   // backgroundColor: 'linear-gradient(0deg,red 50%,green 50%)',
          //   // textColor: '#142F48',
          //   isCloseable: true
          //
          // },
          navbar: {
            title: 'Simple Robot | Mirai组件',
            logo: {
              alt: 'Simbot Logo',
              src: 'img/favicon.png',
            },
            items: [
              {
                type: 'doc',
                docId: 'home',
                position: 'left',
                label: '文档',
              },
              {href: 'https://simbot.forte.love', label: 'simbot官网', position: 'left'},
              {href: 'https://github.com/orgs/simple-robot/discussions', label: '社区', position: 'left'},
              {href: 'https://docs.simbot.forte.love', label: 'API文档', position: 'left'},
              {
                type: 'docsVersionDropdown',
                position: 'right',
                docsPluginId: 'default',
                // dropdownItemsAfter: [{to: '/versions', label: 'All versions'}],
                dropdownActiveClassDisabled: true,
              },
              {
                href: 'https://github.com/simple-robot/simbot-component-mirai',
                position: 'right',
                className: 'bi-github',
                'aria-label': 'GitHub',
              },
            ],
          },
          footer: {
            style: 'dark',
            links: [
              {
                title: '文档',
                items: [
                  {
                    label: '文档',
                    to: '/docs',
                  },
                  {
                    label: 'API文档',
                    to: 'https://docs.simbot.forte.love',
                  },
                ],
              },
              {
                title: '指路牌',
                items: [
                  {
                    label: 'GitHub ',
                    href: 'https://github.com/simple-robot/simbot-component-mirai',
                  },
                  {
                    label: 'simbot官网 🏠',
                    href: 'https://simbot.forte.love',
                  },
                  {
                    label: '组织库 🏢',
                    href: 'https://github.com/simple-robot',
                  },
                  {
                    label: '图书馆 📚',
                    href: 'https://github.com/simple-robot-library',
                  },
                ],
              },
              {
                title: '交流&反馈',
                items: [
                  {
                    label: '问题反馈',
                    href: 'https://github.com/simple-robot/simpler-robot/issues',
                  },
                  {
                    label: '交流社区',
                    href: 'https://github.com/orgs/simple-robot/discussions',
                  },
                ],
              },
            ],
            copyright: `Built with <a href="https://www.docusaurus.io/zh-CN/">Docusaurus</a>. <br> Copyright © ${copyrightYear} Forte Scarlet.`,
          },
          prism: {
            additionalLanguages: ['java', 'kotlin', 'groovy', 'gradle', 'properties'],
            theme: lightCodeTheme,
            darkTheme: darkCodeTheme,
            magicComments: [
              // Remember to extend the default highlight class name as well!
              {
                className: 'theme-code-block-highlighted-line',
                line: 'highlight-next-line',
                block: {start: 'highlight-start', end: 'highlight-end'},
              },
              {
                className: 'code-block-error-line',
                line: 'This will error',
                block: {start: 'error-start', end: 'error-end'},
              },
              {
                className: 'code-block-success-line',
                line: 'This is success',
                block: {start: 'success-start', end: 'success-end'},
              },
            ],
          },
        }),
  }
}

module.exports = config;
