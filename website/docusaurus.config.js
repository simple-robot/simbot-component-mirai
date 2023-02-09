// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

const currentVersion = '3.0.0.0-M4'


/** @type {import('@docusaurus/types').Config} */
async function config() {
  return {
    title: 'simbot-mirai组件库',
    tagline: `基于mirai框架的simbot组件实现库`,
    favicon: 'img/favicon.png',

    // Set the production url of your site here
    url: 'https://component-mirai.simbot.forte.love',
    // Set the /<baseUrl>/ pathname under which your site is served
    // For GitHub pages deployment, it is often '/<projectName>/'
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
            name: 'keywords', content: 'forte, forte-scarlet, fortescarlet, simbot, simple-robot, bot, qqbot, mirai, simbot-mirai'
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
            title: 'Simple Robot Component Mirai',
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
              // {to: '/blog', label: 'Blog', position: 'left'},
              {
                href: 'https://github.com/simple-robot/simbot-component-mirai',
                // label: 'GitHub',
                position: 'right',
                className: 'bi-github',
                'aria-label': 'GitHub',
              },
              {
                type: 'docsVersionDropdown',
                position: 'right',
                docsPluginId: 'default',
                // dropdownItemsAfter: [{to: '/versions', label: 'All versions'}],
                dropdownActiveClassDisabled: true,
              },
            ],
          },
          footer: {
            style: 'dark',
            links: [
              {
                title: 'Docs',
                items: [
                  {
                    label: 'Tutorial',
                    to: '/',
                  },
                ],
              },
              {
                title: 'Community',
                items: [
                  {
                    label: 'Stack Overflow',
                    href: 'https://stackoverflow.com/questions/tagged/docusaurus',
                  },
                  {
                    label: 'Discord',
                    href: 'https://discordapp.com/invite/docusaurus',
                  },
                  {
                    label: 'Twitter',
                    href: 'https://twitter.com/docusaurus',
                  },
                ],
              },
              {
                title: 'More',
                items: [
                  {
                    label: 'GitHub',
                    href: 'https://github.com/simple-robot/simbot-component-mirai',
                  },

                ],
              },
            ],
            copyright: `Copyright © 2022-${new Date().getFullYear()} My Project, Inc. Built with Docusaurus.`,
          },
          prism: {
            theme: lightCodeTheme,
            darkTheme: darkCodeTheme,
          },
        }),
  }
}

module.exports = config;
