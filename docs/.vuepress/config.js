// 引入JSON文件
const sidebarCn = require('./sidebar-cn.json');
const sidebarEn = require('./sidebar-en.json');
const navEn = require('./nav-en.json');
module.exports = {
  base: '/tools-ocr/',
  title: 'Tools OCR',
  description: 'Tools OCR',
  head: [
    ["link", {
      rel: "icon",
      href: '/favicon.ico'
    }],
    ["meta", {
      name: "author",
      content: "litongjava@qq.com,jfinal@qq.com"
    }],
    ["meta", {
      name: "keywords",
      content: "tools-ocr,ocr tools"
    }],
    ["script", {
      "crossorigin": "anonymous",
      async: true,
      src: ""
    }],
  ],

  markdown: {
    lineNumbers: true
  },
  themeConfig: {
    logo: '/jfinallogo.png',
    lastUpdated: 'Last Updated', // string | boolean,K
    nav: navEn,
    sidebar: {
      '/cn/': sidebarCn,
      '/en/': sidebarEn
    }
  },
}