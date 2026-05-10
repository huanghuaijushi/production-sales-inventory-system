import { defineConfig, type PluginOption } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'

type UniPlugin = () => PluginOption | PluginOption[]

process.env.UNI_PLATFORM ||= 'app'

const uniCompat = uniModule as unknown as UniPlugin | { default: UniPlugin }
const uni = typeof uniCompat === 'function' ? uniCompat : uniCompat.default

export default defineConfig({
  plugins: [uni()]
})
