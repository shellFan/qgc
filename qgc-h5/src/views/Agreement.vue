<template>
  <div class="agreement-page">
    <van-nav-bar left-arrow @click-left="$router.back()" title="协议与政策" fixed placeholder />

    <van-tabs v-model:active="activeTab" sticky offset-top="46">
      <van-tab title="用户协议" name="agreement">
        <div class="content-wrap">
          <h3>用户协议</h3>
          <p class="update-time">更新日期：2026年1月1日</p>

          <h4>一、总则</h4>
          <p>欢迎您使用穷鬼筹（以下简称"本平台"）。请您在使用本平台前，仔细阅读本协议的全部内容。如您对本协议有任何疑问，可通过本平台客服渠道进行咨询。</p>
          <p>您一旦使用本平台服务，即视为您已阅读并同意本协议的约束。本平台有权在必要时修改本协议内容，修改后的协议一经公布即有效替代原协议。</p>

          <h4>二、服务内容</h4>
          <p>本平台为用户提供筹款信息发布、在线支付、社交分享等互联网信息服务。用户可以通过本平台发起筹款项目、参与支持他人的筹款项目、发布返图证明等。</p>
          <p>本平台保留随时变更、中断或终止部分或全部服务的权利。</p>

          <h4>三、用户注册与账号</h4>
          <p>用户须通过微信授权方式注册并登录本平台。用户应提供真实、准确、完整的注册信息，并对所提供信息承担全部责任。</p>
          <p>用户应妥善保管账号信息，因用户个人原因导致账号泄露所引起的一切损失由用户自行承担。</p>

          <h4>四、用户行为规范</h4>
          <p>用户在使用本平台时，必须遵守中华人民共和国相关法律法规，不得利用本平台从事任何违法违规活动，包括但不限于：</p>
          <ul>
            <li>发布虚假筹款信息或诈骗内容</li>
            <li>发布涉及政治敏感、色情、暴力等违法信息</li>
            <li>侵犯他人知识产权或其他合法权益</li>
            <li>恶意刷单、套现等扰乱平台秩序的行为</li>
            <li>利用技术手段攻击平台系统或数据</li>
          </ul>

          <h4>五、筹款规则</h4>
          <p>发起人应确保筹款信息的真实性，不得虚构筹款事由。筹款到期后，平台将按照规则将筹集款项划转至发起人钱包。</p>
          <p>支持者一旦完成支付，除法律规定的情形外，不支持退款。</p>

          <h4>六、免责声明</h4>
          <p>本平台仅提供信息发布和技术服务，不对筹款项目的真实性、合法性作出保证。因筹款项目引发的纠纷，由发起人与支持者自行解决，本平台不承担任何责任。</p>
          <p>因不可抗力、系统故障等原因导致服务中断，本平台不承担责任，但将尽快恢复服务。</p>

          <h4>七、知识产权</h4>
          <p>本平台所有内容（包括但不限于文字、图片、软件、界面设计等）的知识产权均归本平台所有，未经授权不得复制、传播或用于商业用途。</p>
          <p>用户在本平台发布的内容，视为授予本平台免费的、永久的使用许可。</p>
        </div>
      </van-tab>

      <van-tab title="隐私政策" name="privacy">
        <div class="content-wrap">
          <h3>隐私政策</h3>
          <p class="update-time">更新日期：2026年1月1日</p>

          <h4>一、信息收集</h4>
          <p>本平台收集和使用您的个人信息，旨在为您提供更好的服务。我们收集的信息包括：</p>
          <ul>
            <li><strong>注册信息：</strong>微信授权获取的昵称、头像、OpenID等</li>
            <li><strong>交易信息：</strong>您的支付记录、提现记录等</li>
            <li><strong>设备信息：</strong>设备型号、操作系统版本等</li>
            <li><strong>日志信息：</strong>访问时间、页面浏览记录等</li>
          </ul>

          <h4>二、信息使用</h4>
          <p>我们收集的信息将用于：</p>
          <ul>
            <li>为您提供筹款、支付等核心服务</li>
            <li>身份验证与安全防护</li>
            <li>改进产品体验与服务质量</li>
            <li>遵守法律法规要求</li>
          </ul>

          <h4>三、信息共享</h4>
          <p>未经您的同意，本平台不会向任何第三方共享您的个人信息，但以下情形除外：</p>
          <ul>
            <li>法律法规要求或司法、行政机关依法强制要求</li>
            <li>与关联方共享，但仅限于本政策声明的目的</li>
            <li>与授权合作伙伴共享，仅为实现本政策声明的目的</li>
          </ul>

          <h4>四、信息安全</h4>
          <p>我们采用业界通行的安全技术来保护您的个人信息，防止数据遭到未经授权的访问、披露、使用、修改或损毁。</p>

          <h4>五、您的权利</h4>
          <p>您有权访问、更正、删除您的个人信息。如需行使相关权利，可通过平台客服渠道联系我们。</p>

          <h4>六、未成年人保护</h4>
          <p>本平台不面向未满14周岁的未成年人提供服务。如果我们发现在未事先获得可证实的监护人同意的情况下收集了未成年人的个人信息，会设法尽快删除相关数据。</p>

          <h4>七、政策更新</h4>
          <p>本隐私政策可能会不时更新。更新后的政策将在本页面发布，重大变更会通过平台通知等方式告知您。</p>
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref('agreement')

onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
})
</script>

<style scoped>
.agreement-page {
  min-height: 100dvh;
  background: var(--qgc-bg);
}
.content-wrap {
  padding: var(--qgc-spacing-lg);
  background: var(--qgc-bg-white);
  margin: var(--qgc-spacing-sm) var(--qgc-spacing-md);
  border-radius: var(--qgc-radius-md);
  box-shadow: var(--qgc-shadow-sm);
}
.content-wrap h3 {
  font-size: var(--qgc-font-xl);
  font-weight: 700;
  color: var(--qgc-primary);
  margin: 0 0 4px;
}
.content-wrap h4 {
  font-size: var(--qgc-font-md);
  font-weight: 600;
  color: var(--qgc-text-primary);
  margin-top: var(--qgc-spacing-lg);
  margin-bottom: var(--qgc-spacing-xs);
}
.content-wrap p {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-secondary);
  line-height: 1.8;
  margin-bottom: var(--qgc-spacing-xs);
}
.content-wrap ul {
  padding-left: 20px;
  margin-bottom: var(--qgc-spacing-xs);
}
.content-wrap li {
  font-size: var(--qgc-font-md);
  color: var(--qgc-text-secondary);
  line-height: 1.8;
  margin-bottom: 4px;
}
.update-time {
  font-size: var(--qgc-font-xs);
  color: var(--qgc-text-tertiary);
  margin-bottom: var(--qgc-spacing-md) !important;
}
</style>