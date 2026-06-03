import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { requiresAuth: false } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { requiresAuth: false } },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '学习首页' } },
      { path: 'word', name: 'WordStudy', component: () => import('@/views/WordStudy.vue'), meta: { title: '单词学习' } },
      { path: 'practice', name: 'Practice', component: () => import('@/views/Practice.vue'), meta: { title: '题库练习' } },
      { path: 'essay', name: 'AIEssay', component: () => import('@/views/AIEssay.vue'), meta: { title: 'AI作文批改' } },
      { path: 'pronunciation', name: 'Pronunciation', component: () => import('@/views/Pronunciation.vue'), meta: { title: '发音评测' } },
      { path: 'statistics', name: 'Statistics', component: () => import('@/views/Statistics.vue'), meta: { title: '学习统计' } },
      { path: 'profile', name: 'Profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心' } },
      { path: 'wrong-book', name: 'WrongBook', component: () => import('@/views/WrongBook.vue'), meta: { title: '错题本' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/home' }
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  document.title = (to.meta.title || '英语学习') + ' - EnglishLearningPlatform'
  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    next('/home')
  } else {
    next()
  }
})

export default router
