/**
 * GameServer MiniClient GM Workbench (功能协议工作台)
 * 零构建依赖纯原生 JavaScript 实现 (DC-02, DC-03, DC-04, DC-05, DC-12)
 *
 * @author shuyongqiang
 */
(function () {
  'use strict';

  // 全局状态
  const state = {
    catalog: null,
    modules: [],
    currentModule: null,
    currentRequest: null,
    currentPayload: {},
    onlinePlayers: [],
    selectedPlayerId: 0,
    withTrace: true,
    withDiff: true,
    mirrorToRealClient: false,
    timeoutMs: 5000,
    activeSubTab: 'form', // 'form' | 'json'
    activeResultTab: 'response', // 'response' | 'pushes' | 'trace' | 'diff'
    lastResult: null
  };

  function getToken() {
    return (window.localStorage && window.localStorage.getItem('gm_token')) || '';
  }

  async function apiFetch(url, method = 'GET', body = null) {
    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) {
      headers['X-GM-Token'] = token;
      headers['Authorization'] = 'Bearer ' + token;
    }
    const options = { method, headers };
    if (body) {
      options.body = typeof body === 'string' ? body : JSON.stringify(body);
    }
    const res = await fetch(url, options);
    if (!res.ok) {
      let errText = '';
      try { errText = await res.text(); } catch (e) {}
      throw new Error(`HTTP ${res.status}: ${errText || res.statusText}`);
    }
    return await res.json();
  }

  async function initWorkbench() {
    const container = document.getElementById('tabWorkbench');
    if (!container) return;

    window.addEventListener('hashchange', handleRoute);
    await loadCatalog();
    await loadOnlinePlayers();
    handleRoute();
  }

  async function loadCatalog(keyword = '') {
    try {
      const url = '/gm-console/api/workbench/modules' + (keyword ? `?k=${encodeURIComponent(keyword)}` : '');
      const res = await apiFetch(url);
      if (res && res.ok && res.data) {
        state.catalog = res.data;
        state.modules = res.data.modules || [];
      } else {
        state.modules = [];
      }
    } catch (e) {
      console.error('加载工作台模块目录异常', e);
    }
  }

  async function loadOnlinePlayers() {
    try {
      const res = await apiFetch('/gm-console/api/workbench/players');
      if (res && res.ok && res.data) {
        state.onlinePlayers = res.data.players || [];
        if (state.selectedPlayerId === 0 && state.onlinePlayers.length > 0) {
          state.selectedPlayerId = state.onlinePlayers[0].playerId;
        }
      }
    } catch (e) {
      console.warn('获取在线玩家列表失败', e);
    }
  }

  function handleRoute() {
    const hash = window.location.hash || '#/home';
    if (hash.startsWith('#/module/')) {
      const modIdStr = hash.substring('#/module/'.length).trim();
      const modId = (modIdStr.startsWith('0x') || modIdStr.startsWith('0X'))
        ? parseInt(modIdStr, 16)
        : parseInt(modIdStr, 10);
      renderModuleWorkbench(modId);
    } else {
      renderModuleCatalog();
    }
  }

  // 1. 渲染模块列表首页
  function renderModuleCatalog() {
    const container = document.getElementById('tabWorkbench');
    if (!container) return;

    let html = `
      <div class="card">
        <div class="card-title">
          <span>📦 全服功能模块协议工作台</span>
          <div style="display:flex; gap:10px; align-items:center;">
            <input type="text" id="wbSearchInput" placeholder="搜索模块名称 / Key / 协议十六进制 (回车检索)..."
                   style="width:320px;" onkeydown="if(event.key==='Enter') window.WorkbenchApp.search(this.value);" />
            <button class="btn btn-secondary" onclick="window.WorkbenchApp.search(document.getElementById('wbSearchInput').value)">🔍 搜索</button>
            <button class="btn btn-secondary" onclick="window.WorkbenchApp.refresh()">🔄 刷新目录</button>
          </div>
        </div>
        <div style="font-size:0.85rem; color:var(--text-muted); margin-bottom:16px;">
          已索引模块 (@MessageModule): <b>${state.modules.length}</b> 个 | 运行模式: <span class="badge ${state.catalog?.simulationEnabled ? 'badge-dev' : 'badge-prod'}">${state.catalog?.runMode || 'UNKNOWN'}</span>
        </div>
        <div class="grid" style="grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 16px;">
    `;

    if (state.modules.length === 0) {
      html += `<div style="grid-column: 1/-1; text-align:center; padding:40px; color:var(--text-muted);">未找到声明了 @MessageModule 的匹配功能模块</div>`;
    } else {
      for (const m of state.modules) {
        const reqCount = (m.requests || []).length;
        const pushCount = (m.pushes || []).length;
        const actCount = (m.actions || []).length;
        const fnBadge = m.functionName ? ` | 功能: <code>${escapeHtml(m.functionName)}</code>` : '';
        html += `
          <div class="card" style="margin:0; cursor:pointer; transition:transform 0.15s, border-color 0.15s;"
               onmouseover="this.style.borderColor='var(--primary)'" onmouseout="this.style.borderColor='var(--border-color)'"
               onclick="window.location.hash='#/module/${m.moduleId}'">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
              <span style="font-weight:700; color:var(--primary); font-size:1.05rem;">${escapeHtml(m.title)}</span>
              <span class="badge badge-dev">${escapeHtml(m.moduleIdHex)}</span>
            </div>
            <div style="font-size:0.85rem; color:var(--text-muted); margin-bottom:8px;">
              Key: <code>${escapeHtml(m.moduleKey)}</code>${fnBadge}
            </div>
            <div style="display:flex; gap:12px; font-size:0.8rem; color:#94a3b8;">
              <span>⚡ 请求: <b>${reqCount}</b></span>
              <span>📡 推送: <b>${pushCount}</b></span>
              <span>🛠️ 快捷GM: <b>${actCount}</b></span>
            </div>
          </div>
        `;
      }
    }

    html += `
        </div>
      </div>
    `;

    container.innerHTML = html;
  }

  // 2. 渲染单个模块的工作台
  function renderModuleWorkbench(moduleId) {
    const container = document.getElementById('tabWorkbench');
    if (!container) return;

    const mod = state.modules.find(m => m.moduleId === moduleId);
    if (!mod) {
      container.innerHTML = `
        <div class="card">
          <button class="btn btn-secondary" onclick="window.location.hash='#/home'">← 返回模块列表</button>
          <div style="padding:20px; color:var(--danger)">未找到 ID 为 ${escapeHtml(String(moduleId))} 的功能模块</div>
        </div>
      `;
      return;
    }
    state.currentModule = mod;

    if (!state.currentRequest || !mod.requests.some(r => r.msgId === state.currentRequest.msgId)) {
      state.currentRequest = mod.requests.find(r => r.simulatable) || mod.requests[0] || null;
      state.currentPayload = {};
    }

    let html = `
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
        <div style="display:flex; align-items:center; gap:12px;">
          <button class="btn btn-secondary" onclick="window.location.hash='#/home'">← 模块列表</button>
          <h2 style="font-size:1.3rem; color:var(--primary); margin:0;">${escapeHtml(mod.title)}</h2>
          <span class="badge badge-dev">${escapeHtml(mod.moduleIdHex)}</span>
          <span style="font-size:0.85rem; color:var(--text-muted);">(${escapeHtml(mod.moduleKey)})</span>
        </div>
        <div style="display:flex; align-items:center; gap:8px;">
          <label style="font-size:0.85rem; margin:0;">调试玩家:</label>
          <select id="wbPlayerSelect" style="width:220px;" onchange="window.WorkbenchApp.onPlayerChange(this.value)">
            ${renderPlayerOptions()}
          </select>
          <button class="btn btn-secondary" style="padding:6px 10px; font-size:0.8rem;" onclick="window.WorkbenchApp.quickTestLogin()">➕ 测试登录</button>
        </div>
      </div>

      <div style="display:grid; grid-template-columns: 320px 1fr; gap:16px;">
        <!-- 左侧导航: 协议列表、推送、错误码、GM动作 -->
        <div class="card" style="margin:0; padding:14px; max-height:800px; overflow-y:auto;">
          <div style="font-size:0.9rem; font-weight:700; color:var(--primary); margin-bottom:10px;">⚡ 业务请求 (Requests)</div>
          <div style="display:flex; flex-direction:column; gap:6px; margin-bottom:20px;">
            ${renderRequestList(mod.requests)}
          </div>

          <div style="font-size:0.9rem; font-weight:700; color:#38bdf8; margin-bottom:8px;">📡 模块推送 (Pushes)</div>
          <div style="display:flex; flex-direction:column; gap:4px; margin-bottom:20px; font-size:0.85rem;">
            ${(mod.pushes || []).map(p => `
              <div style="padding:4px 8px; background:#0f172a; border-radius:4px; border:1px solid var(--border-color);">
                <span style="color:#67e8f9">${escapeHtml(p.msgName)}</span>
                <span style="color:var(--text-muted); font-size:0.75rem; float:right;">${escapeHtml(p.msgIdHex)}</span>
              </div>
            `).join('') || '<div style="color:var(--text-muted); font-size:0.8rem;">无推送协议</div>'}
          </div>

          <div style="font-size:0.9rem; font-weight:700; color:#f59e0b; margin-bottom:8px;">🛠️ 关联 GM 动作</div>
          <div style="display:flex; flex-direction:column; gap:4px; margin-bottom:20px; font-size:0.85rem;">
            ${(mod.actions || []).map(act => `
              <button class="btn btn-secondary" style="padding:4px 8px; font-size:0.8rem; justify-content:space-between;"
                      onclick="window.WorkbenchApp.fillGmAction('${escapeHtml(act.action)}')">
                <span>${escapeHtml(act.description || act.action)}</span>
                <span style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(act.action)}</span>
              </button>
            `).join('') || '<div style="color:var(--text-muted); font-size:0.8rem;">无关联GM动作</div>'}
          </div>

          <div style="font-size:0.9rem; font-weight:700; color:#ef4444; margin-bottom:8px;">⚠️ 模块错误码</div>
          <div style="display:flex; flex-direction:column; gap:4px; font-size:0.8rem;">
            ${(mod.errors || []).map(err => `
              <div style="padding:4px 6px; background:#0f172a; border-radius:4px; border:1px solid var(--border-color);">
                <div style="color:#f87171; font-weight:600;">${escapeHtml(err.name)} (${err.id})</div>
                <div style="color:var(--text-muted); font-size:0.75rem;">${escapeHtml(err.desc || '')}</div>
              </div>
            `).join('') || '<div style="color:var(--text-muted); font-size:0.8rem;">无专属错误码</div>'}
          </div>
        </div>

        <!-- 右侧主区: 动态表单 / JSON、模拟发包、响应/Trace/Diff面板 -->
        <div>
          ${renderProtocolWorkbenchMain()}
        </div>
      </div>
    `;

    container.innerHTML = html;
  }

  function renderPlayerOptions() {
    let opts = `<option value="0" ${state.selectedPlayerId === 0 ? 'selected' : ''}>-- 选择在线玩家 --</option>`;
    for (const p of state.onlinePlayers) {
      const isSel = p.playerId === state.selectedPlayerId ? 'selected' : '';
      const tag = p.hasRealWsClient ? '真实客户端' : (p.isVirtualClient ? '虚拟客户端' : '在线');
      opts += `<option value="${p.playerId}" ${isSel}>${p.playerId} - ${escapeHtml(p.name || '未命名')} (Lv.${p.level}) [${tag}]</option>`;
    }
    return opts;
  }

  function renderRequestList(requests) {
    if (!requests || requests.length === 0) {
      return `<div style="color:var(--text-muted); font-size:0.85rem;">本模块未定义 Request 协议</div>`;
    }
    return requests.map(r => {
      const isSelected = state.currentRequest && state.currentRequest.msgId === r.msgId;
      const border = isSelected ? 'border-color:var(--primary); background:#172554;' : 'border-color:var(--border-color); background:#0f172a;';
      const disabledBadge = !r.simulatable ? `<span class="badge badge-prod" style="font-size:0.65rem;" title="${escapeHtml(r.unsupportedReason || '')}">不可模拟</span>` : '';
      const commentLine = r.comment ? `<div style="font-size:0.75rem; color:#94a3b8; margin-top:2px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="${escapeHtml(r.comment)}">${escapeHtml(r.comment)}</div>` : '';
      return `
        <div style="padding:8px 10px; border-radius:6px; border:1px solid; ${border} cursor:pointer;"
             onclick="window.WorkbenchApp.selectRequest(${r.msgId})">
          <div style="display:flex; justify-content:space-between; align-items:center;">
            <span style="font-size:0.85rem; font-weight:600; color:${isSelected ? '#67e8f9' : '#f8fafc'};">${escapeHtml(r.msgName)}</span>
            ${disabledBadge}
          </div>
          ${commentLine}
          <div style="display:flex; justify-content:space-between; font-size:0.75rem; color:var(--text-muted); margin-top:4px;">
            <span>${escapeHtml(r.msgIdHex)}</span>
            <span>${r.pairedResponseName ? escapeHtml(r.pairedResponseName) : '无配对响应'}</span>
          </div>
        </div>
      `;
    }).join('');
  }
  function getValueAtPath(root, path) {
    if (!root || !path) return undefined;
    const parts = path.replace(/\[(\d+)\]/g, '.$1').split('.');
    let cur = root;
    for (const part of parts) {
      if (cur == null) return undefined;
      cur = cur[part];
    }
    return cur;
  }

  function setValueAtPath(root, path, value) {
    const parts = path.replace(/\[(\d+)\]/g, '.$1').split('.');
    let cur = root;
    for (let i = 0; i < parts.length - 1; i++) {
      const key = parts[i];
      const nextKey = parts[i + 1];
      const nextIsIdx = /^\d+$/.test(nextKey);
      if (cur[key] == null || typeof cur[key] !== 'object') {
        cur[key] = nextIsIdx ? [] : {};
      }
      cur = cur[key];
    }
    cur[parts[parts.length - 1]] = value;
  }

  function stripEnumSuffix(enumText) {
    return String(enumText || '').replace(/\(\-?\d+\)$/, '').trim();
  }

  function renderProtocolWorkbenchMain() {
    const req = state.currentRequest;
    if (!req) {
      return `<div class="card"><div style="padding:30px; text-align:center; color:var(--text-muted);">请在左侧选择要模拟的 Request 协议</div></div>`;
    }

    const isSimulatable = req.simulatable !== false;
    const reqCommentHtml = req.comment
      ? `<div style="font-size:0.85rem; color:#94a3b8; margin:-6px 0 12px 0;">📝 ${escapeHtml(req.comment)}</div>`
      : '';

    return `
      <!-- 协议发包面板 -->
      <div class="card" style="margin-bottom:16px;">
        <div class="card-title">
          <div>
            <span>${escapeHtml(req.msgName)}</span>
            <span class="badge badge-dev" style="margin-left:8px;">${escapeHtml(req.msgIdHex)}</span>
            ${req.pairedResponseName ? `<span style="font-size:0.85rem; color:var(--text-muted); margin-left:10px;">➔ ${escapeHtml(req.pairedResponseName)} (${escapeHtml(req.pairedResponseHex)})</span>` : ''}
          </div>
          <div style="display:flex; gap:8px;">
            <button class="btn btn-secondary" style="padding:4px 10px; font-size:0.8rem;" onclick="window.WorkbenchApp.saveTemplate()">💾 存为模板</button>
            <button class="btn btn-secondary" style="padding:4px 10px; font-size:0.8rem;" onclick="window.WorkbenchApp.loadTemplate()">📂 读取模板</button>
            <button class="btn btn-secondary" style="padding:4px 10px; font-size:0.8rem;" onclick="window.WorkbenchApp.copyCurl()">📋 复制 cURL</button>
            <button class="btn btn-secondary" style="padding:4px 10px; font-size:0.8rem;" onclick="window.WorkbenchApp.copyJavaSnippet()">☕ 导出 Java 测试</button>
          </div>
        </div>
        ${reqCommentHtml}

        ${!isSimulatable ? `
          <div style="padding:10px 14px; background:#7f1d1d; border-radius:6px; margin-bottom:14px; font-size:0.85rem; color:#fee2e2;">
            ⛔ <b>不可模拟协议:</b> ${escapeHtml(req.unsupportedReason || '该协议不支持直接模拟')}
          </div>
        ` : ''}

        <!-- 调试选项条 -->
        <div style="display:flex; flex-wrap:wrap; gap:16px; align-items:center; background:#0f172a; padding:10px 14px; border-radius:6px; border:1px solid var(--border-color); margin-bottom:14px; font-size:0.85rem;">
          <label style="display:flex; align-items:center; gap:6px; margin:0; cursor:pointer;">
            <input type="checkbox" id="wbWithTrace" ${state.withTrace ? 'checked' : ''} onchange="state.withTrace=this.checked" style="width:auto;" />
            <span>开启隔离 Trace 录制</span>
          </label>
          <label style="display:flex; align-items:center; gap:6px; margin:0; cursor:pointer;">
            <input type="checkbox" id="wbWithDiff" ${state.withDiff ? 'checked' : ''} onchange="state.withDiff=this.checked" style="width:auto;" />
            <span>开启前后状态 Diff</span>
          </label>
          <label style="display:flex; align-items:center; gap:6px; margin:0; cursor:pointer;" title="开启后将回包同时镜像下发至在线真实客户端WebSocket">
            <input type="checkbox" id="wbMirrorReal" ${state.mirrorToRealClient ? 'checked' : ''} onchange="state.mirrorToRealClient=this.checked" style="width:auto;" />
            <span>镜像至真实客户端</span>
          </label>
          <div style="display:flex; align-items:center; gap:6px;">
            <span>超时:</span>
            <input type="number" id="wbTimeoutMs" value="${state.timeoutMs}" style="width:70px; padding:2px 6px;" onchange="state.timeoutMs=parseInt(this.value,10)||5000" />
            <span>ms</span>
          </div>
        </div>

        <!-- 表单与 JSON 视图切换 -->
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
          <div style="display:flex; gap:8px;">
            <button class="tab-btn ${state.activeSubTab === 'form' ? 'active' : ''}" style="padding:4px 12px; font-size:0.85rem;" onclick="window.WorkbenchApp.switchSubTab('form')">表单视图</button>
            <button class="tab-btn ${state.activeSubTab === 'json' ? 'active' : ''}" style="padding:4px 12px; font-size:0.85rem;" onclick="window.WorkbenchApp.switchSubTab('json')">JSON 视图</button>
          </div>
          <button class="btn btn-secondary" style="padding:4px 10px; font-size:0.75rem;" onclick="window.WorkbenchApp.resetForm()">清空表单</button>
        </div>

        <!-- 表单区 -->
        <div id="wbFormContainer" style="display:${state.activeSubTab === 'form' ? 'block' : 'none'};">
          ${renderProtobufForm(req.reqSchema)}
        </div>

        <!-- JSON 视图区 -->
        <div id="wbJsonContainer" style="display:${state.activeSubTab === 'json' ? 'block' : 'none'};">
          <textarea id="wbJsonTextarea" rows="10" style="font-family:monospace; font-size:0.85rem; width:100%;"
                    onblur="window.WorkbenchApp.onJsonBlur(this.value)">${escapeHtml(JSON.stringify(state.currentPayload, null, 2))}</textarea>
        </div>

        <div style="margin-top:16px; display:flex; gap:12px;">
          <button class="btn" style="flex:1; justify-content:center; padding:10px; font-size:1rem;"
                  ${!isSimulatable ? 'disabled' : ''} onclick="window.WorkbenchApp.invoke()">
            🚀 发起模拟调用 (Invoke)
          </button>
        </div>
      </div>

      <!-- 执行结果、推送、Trace 与 Diff 检视面板 -->
      <div class="card">
        <div class="card-title">
          <span>📊 执行与捕获检视 (Capture Outcome)</span>
          <div style="display:flex; gap:6px;">
            <button class="tab-btn ${state.activeResultTab === 'response' ? 'active' : ''}" style="padding:4px 12px; font-size:0.85rem;" onclick="window.WorkbenchApp.switchResultTab('response')">响应 Response</button>
            <button class="tab-btn ${state.activeResultTab === 'pushes' ? 'active' : ''}" style="padding:4px 12px; font-size:0.85rem;" onclick="window.WorkbenchApp.switchResultTab('pushes')">推送 Pushes</button>
            <button class="tab-btn ${state.activeResultTab === 'trace' ? 'active' : ''}" style="padding:4px 12px; font-size:0.85rem;" onclick="window.WorkbenchApp.switchResultTab('trace')">链路 Trace</button>
            <button class="tab-btn ${state.activeResultTab === 'diff' ? 'active' : ''}" style="padding:4px 12px; font-size:0.85rem;" onclick="window.WorkbenchApp.switchResultTab('diff')">数据 Diff</button>
          </div>
        </div>

        <div id="wbResultContent">
          ${renderResultTabContent()}
        </div>
      </div>
    `;
  }

  // 3. 动态 Protobuf 表单生成器
  function renderProtobufForm(schema, pathPrefix = '') {
    if (!schema) {
      return `<div style="color:var(--text-muted); font-size:0.85rem; padding:12px; background:#0f172a; border-radius:6px;">该协议无 Schema 定义 (可在 JSON 视图手动编辑)</div>`;
    }
    if (schema.truncated) {
      const existing = getValueAtPath(state.currentPayload, pathPrefix);
      const rawJson = existing !== undefined ? JSON.stringify(existing, null, 2) : '{}';
      return `
        <div style="padding:10px; background:#1e1b4b; border:1px solid #4338ca; border-radius:6px;">
          <div style="font-size:0.8rem; color:#c7d2fe; margin-bottom:6px;">⚠️ 嵌套层级超过上限，已自动降级为 JSON 片段录入:</div>
          <textarea rows="4" style="font-family:monospace; font-size:0.8rem; width:100%;"
                    onchange="window.WorkbenchApp.updateJsonField('${escapeHtml(pathPrefix)}', this.value)">${escapeHtml(rawJson)}</textarea>
        </div>
      `;
    }
    if (!schema.fields || schema.fields.length === 0) {
      return `<div style="color:var(--text-muted); font-size:0.85rem; padding:12px; background:#0f172a; border-radius:6px;">该协议无需输入参数 (Empty Message)</div>`;
    }

    let html = `<div style="display:flex; flex-direction:column; gap:12px;">`;

    for (const f of schema.fields) {
      const fieldPath = pathPrefix ? `${pathPrefix}.${f.name}` : f.name;
      const commentHtml = f.comment ? `<span style="font-size:0.75rem; color:var(--text-muted); margin-left:8px;">// ${escapeHtml(f.comment)}</span>` : '';
      const curVal = getValueAtPath(state.currentPayload, fieldPath);

      html += `<div class="form-group" style="margin-bottom:8px;">`;
      html += `<label style="font-weight:600; font-size:0.85rem;">${escapeHtml(f.name)} <span style="font-size:0.75rem; color:#38bdf8;">(${escapeHtml(f.protoType || f.javaType)}${f.repeated && !f.mapField ? '[]' : ''})</span>${commentHtml}</label>`;

      if (f.mapField) {
        const mapJson = (curVal && typeof curVal === 'object') ? JSON.stringify(curVal, null, 2) : '{}';
        html += `
          <textarea rows="3" placeholder='输入 Map JSON，例如 {"1": 100}'
                    style="font-family:monospace; font-size:0.8rem; width:100%;"
                    onchange="window.WorkbenchApp.updateJsonField('${escapeHtml(fieldPath)}', this.value)">${escapeHtml(mapJson)}</textarea>
        `;
      } else if (f.repeated) {
        html += renderRepeatedFieldInput(f, fieldPath, curVal);
      } else if (f.javaType === 'ENUM' && f.enumValues) {
        html += renderScalarOrEnumInput(f, fieldPath, curVal);
      } else if (f.javaType === 'MESSAGE' && f.nestedSchema) {
        html += `
          <div style="padding:10px 14px; background:#0b1120; border:1px solid var(--border-color); border-radius:6px; margin-top:4px;">
            ${renderProtobufForm(f.nestedSchema, fieldPath)}
          </div>
        `;
      } else {
        html += renderScalarOrEnumInput(f, fieldPath, curVal);
      }

      html += `</div>`;
    }

    html += `</div>`;
    return html;
  }

  function renderScalarOrEnumInput(f, fieldPath, curVal) {
    const safePath = escapeHtml(fieldPath);
    if (f.javaType === 'ENUM' && f.enumValues) {
      const selStr = curVal != null ? String(curVal) : '';
      return `
        <select style="width:100%;" onchange="window.WorkbenchApp.updateField('${safePath}', this.value)">
          <option value="">-- 请选择 --</option>
          ${f.enumValues.map(v => {
            const enumName = stripEnumSuffix(v);
            const selected = (selStr === enumName || selStr === v) ? 'selected' : '';
            return `<option value="${escapeHtml(enumName)}" ${selected}>${escapeHtml(v)}</option>`;
          }).join('')}
        </select>
      `;
    }
    if (f.javaType === 'BOOLEAN') {
      const boolVal = curVal === true || curVal === 'true';
      return `
        <select style="width:100%;" onchange="window.WorkbenchApp.updateField('${safePath}', this.value === 'true')">
          <option value="false" ${!boolVal ? 'selected' : ''}>false</option>
          <option value="true" ${boolVal ? 'selected' : ''}>true</option>
        </select>
      `;
    }
    if (f.javaType === 'INT') {
      const valAttr = curVal !== undefined && curVal !== null ? `value="${escapeHtml(String(curVal))}"` : '';
      return `<input type="number" ${valAttr} placeholder="输入整数 (${escapeHtml(f.protoType || 'int32')})..." onchange="window.WorkbenchApp.updateField('${safePath}', this.value === '' ? undefined : (parseInt(this.value, 10) || 0))" />`;
    }
    if (f.javaType === 'LONG') {
      const valAttr = curVal !== undefined && curVal !== null ? `value="${escapeHtml(String(curVal))}"` : '';
      return `<input type="text" ${valAttr} placeholder="输入 64 位整数 (${escapeHtml(f.protoType || 'int64')})..." onchange="window.WorkbenchApp.updateField('${safePath}', this.value === '' ? undefined : this.value)" />`;
    }
    if (f.javaType === 'FLOAT' || f.javaType === 'DOUBLE') {
      const valAttr = curVal !== undefined && curVal !== null ? `value="${escapeHtml(String(curVal))}"` : '';
      return `<input type="number" step="any" ${valAttr} placeholder="输入浮点数..." onchange="window.WorkbenchApp.updateField('${safePath}', this.value === '' ? undefined : (parseFloat(this.value) || 0.0))" />`;
    }
    const ph = f.javaType === 'BYTE_STRING' ? '输入 Base64 字节串...' : '输入文本...';
    const valAttr = curVal !== undefined && curVal !== null ? `value="${escapeHtml(String(curVal))}"` : '';
    return `<input type="text" ${valAttr} placeholder="${ph}" onchange="window.WorkbenchApp.updateField('${safePath}', this.value)" />`;
  }

  function renderRepeatedFieldInput(f, fieldPath, curArr) {
    const arr = Array.isArray(curArr) ? curArr : [];
    const safePath = escapeHtml(fieldPath);
    let rowsHtml = '';
    for (let i = 0; i < arr.length; i++) {
      const itemPath = `${fieldPath}[${i}]`;
      const inner = (f.javaType === 'MESSAGE' && f.nestedSchema)
        ? `<div style="flex:1; padding:8px; background:#0f172a; border:1px solid var(--border-color); border-radius:4px;">${renderProtobufForm(f.nestedSchema, itemPath)}</div>`
        : `<div style="flex:1;">${renderScalarOrEnumInput(f, itemPath, arr[i])}</div>`;
      rowsHtml += `
        <div style="display:flex; gap:8px; align-items:flex-start;">
          <span style="font-size:0.75rem; color:var(--text-muted); padding-top:6px;">#${i}</span>
          ${inner}
          <button class="btn btn-secondary" style="padding:4px 8px; font-size:0.75rem; color:#f87171;"
                  onclick="window.WorkbenchApp.removeRepeatedItem('${safePath}', ${i})">✕</button>
        </div>
      `;
    }
    return `
      <div style="background:#0b1120; padding:10px; border-radius:6px; border:1px solid var(--border-color);">
        <div style="display:flex; flex-direction:column; gap:8px; margin-bottom:${arr.length > 0 ? '8px' : '0'};">
          ${rowsHtml}
        </div>
        <button class="btn btn-secondary" style="padding:2px 8px; font-size:0.75rem; margin-top:${arr.length > 0 ? '4px' : '0'};"
                onclick="window.WorkbenchApp.addRepeatedItem('${safePath}', '${escapeHtml(f.javaType || 'STRING')}')">+ 添加一项</button>
      </div>
    `;
  }
  // 4. 检视结果渲染
  function renderResultTabContent() {
    const res = state.lastResult;
    if (!res) {
      return `<pre style="color:var(--text-muted); margin:0;">尚未执行模拟调用，请在上方配置参数并点击「发起模拟调用」</pre>`;
    }

    if (state.activeResultTab === 'response') {
      const isOk = res.ok !== false;
      const statusClass = isOk ? 'badge-clock' : 'badge-prod';
      const statusText = isOk ? 'SUCCESS (0 - ok)' : `FAILED (${res.errorCode} - ${res.errorName}: ${res.errorDesc || ''})`;

      let html = `
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
          <div>
            <span class="badge ${statusClass}" style="font-size:0.85rem; padding:4px 10px;">${escapeHtml(statusText)}</span>
            <span style="font-size:0.85rem; color:var(--text-muted); margin-left:10px;">耗时: <b>${res.elapsedMs || 0}ms</b></span>
          </div>
          <span style="font-size:0.8rem; color:var(--text-muted);">目标玩家: <code>${res.playerId || 0}</code></span>
        </div>
      `;

      if (res.errorPush) {
        html += `
          <div style="padding:10px 14px; background:#450a0a; border:1px solid #b91c1c; border-radius:6px; margin-bottom:12px;">
            <div style="color:#f87171; font-weight:700; margin-bottom:4px;">🚨 业务异常推送 (PlayerErrorPush_01000099)</div>
            <pre style="margin:0; background:transparent; border:none; padding:0; color:#fca5a5;">${escapeHtml(JSON.stringify(res.errorPush, null, 2))}</pre>
          </div>
        `;
      }

      if (res.warnings && res.warnings.length > 0) {
        html += `
          <div style="padding:8px 12px; background:#451a03; border:1px solid #d97706; border-radius:6px; margin-bottom:12px; font-size:0.85rem; color:#fde68a;">
            ${res.warnings.map(w => `<div>⚠️ ${escapeHtml(w)}</div>`).join('')}
          </div>
        `;
      }

      html += `<pre style="margin:0;">${escapeHtml(JSON.stringify(res.response, null, 2))}</pre>`;
      return html;
    }

    if (state.activeResultTab === 'pushes') {
      const pushes = res.pushes || [];
      if (pushes.length === 0) {
        return `<pre style="color:var(--text-muted); margin:0;">本次调用未捕获到伴随推送 (Pushes)</pre>`;
      }
      return `
        <div style="display:flex; flex-direction:column; gap:10px;">
          ${pushes.map((p, idx) => `
            <div style="background:#0f172a; border:1px solid var(--border-color); border-radius:6px; padding:10px;">
              <div style="display:flex; justify-content:space-between; margin-bottom:6px;">
                <span style="font-weight:700; color:#38bdf8;">#${p.index || idx + 1} ${escapeHtml(p.msgName)} (${escapeHtml(p.msgIdHex)})</span>
                <span style="color:var(--text-muted); font-size:0.75rem;">seq: ${p.seq}</span>
              </div>
              <pre style="margin:0; max-height:200px;">${escapeHtml(JSON.stringify(p.payload, null, 2))}</pre>
            </div>
          `).join('')}
        </div>
      `;
    }

    if (state.activeResultTab === 'trace') {
      const trace = res.trace;
      if (!trace) {
        return `<pre style="color:var(--text-muted); margin:0;">未启用 Trace 录制或本次调用未生成 Trace 节点</pre>`;
      }
      return `
        <div style="display:flex; flex-direction:column; gap:12px;">
          <div style="display:grid; grid-template-columns:repeat(auto-fit, minmax(200px, 1fr)); gap:10px; background:#0f172a; padding:10px; border-radius:6px;">
            <div>Trace ID: <code>${escapeHtml(trace.traceId || '--')}</code></div>
            <div>耗时: <b>${trace.elapsedMs || 0}ms</b></div>
            <div>错误码: <b>${trace.errorCode || 0}</b></div>
          </div>
          <div>
            <div style="font-weight:700; color:#38bdf8; margin-bottom:6px;">💰 资源流水变动 (${(trace.resourceEvents || []).length})</div>
            ${renderResourceEventsTable(trace.resourceEvents)}
          </div>
          <div>
            <div style="font-weight:700; color:#f87171; margin-bottom:6px;">🚨 ERROR 日志与异常事件 (${(trace.errorEvents || []).length})</div>
            ${renderErrorEventsTable(trace.errorEvents)}
          </div>
        </div>
      `;
    }

    if (state.activeResultTab === 'diff') {
      const diffList = res.diff || [];
      if (diffList.length === 0) {
        return `<pre style="color:var(--text-muted); margin:0;">状态对比无变动 (前后数据快照完全一致)</pre>`;
      }
      return `
        <table style="width:100%; border-collapse:collapse; font-size:0.85rem; font-family:monospace;">
          <thead>
            <tr style="background:#0f172a; border-bottom:1px solid var(--border-color); text-align:left;">
              <th style="padding:8px 12px; color:var(--primary);">字段路径 (Path)</th>
              <th style="padding:8px 12px; color:#f87171;">变更前 (Before)</th>
              <th style="padding:8px 12px; color:#4ade80;">变更后 (After)</th>
            </tr>
          </thead>
          <tbody>
            ${diffList.map(d => `
              <tr style="border-bottom:1px solid #1e293b;">
                <td style="padding:6px 12px; color:#38bdf8;">${escapeHtml(d.path)}</td>
                <td style="padding:6px 12px; color:#fca5a5;">${escapeHtml(JSON.stringify(d.before))}</td>
                <td style="padding:6px 12px; color:#86efac;">${escapeHtml(JSON.stringify(d.after))}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      `;
    }

    return '';
  }

  function renderResourceEventsTable(events) {
    if (!events || events.length === 0) {
      return `<div style="color:var(--text-muted); font-size:0.8rem;">无资源变动</div>`;
    }
    return `
      <table style="width:100%; border-collapse:collapse; font-size:0.8rem;">
        <thead>
          <tr style="background:#0f172a; border-bottom:1px solid var(--border-color); text-align:left;">
            <th style="padding:6px 10px;">道具/资源ID</th>
            <th style="padding:6px 10px;">变动数量</th>
            <th style="padding:6px 10px;">操作类型</th>
          </tr>
        </thead>
        <tbody>
          ${events.map(e => `
            <tr style="border-bottom:1px solid #1e293b;">
              <td style="padding:6px 10px;">${e.id}</td>
              <td style="padding:6px 10px; color:${e.isAdd ? '#4ade80' : '#f87171'}; font-weight:700;">${e.isAdd ? '+' : '-'}${e.count}</td>
              <td style="padding:6px 10px; color:var(--text-muted);">${escapeHtml(e.opType || '')}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  }

  function renderErrorEventsTable(events) {
    if (!events || events.length === 0) {
      return `<div style="color:var(--text-muted); font-size:0.8rem;">无捕获到的 ERROR 日志</div>`;
    }
    return `
      <table style="width:100%; border-collapse:collapse; font-size:0.8rem;">
        <thead>
          <tr style="background:#0f172a; border-bottom:1px solid var(--border-color); text-align:left;">
            <th style="padding:6px 10px;">Logger</th>
            <th style="padding:6px 10px;">异常类</th>
            <th style="padding:6px 10px;">错误内容</th>
          </tr>
        </thead>
        <tbody>
          ${events.map(e => `
            <tr style="border-bottom:1px solid #1e293b;">
              <td style="padding:6px 10px; color:var(--text-muted);">${escapeHtml(e.loggerName || '')}</td>
              <td style="padding:6px 10px; color:#f87171;">${escapeHtml(e.exceptionClass || '')}</td>
              <td style="padding:6px 10px; color:#fca5a5;">${escapeHtml(e.message || '')}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    `;
  }

  function escapeHtml(str) {
    if (str == null) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  // 暴露给 Window 的工作台 API 钩子
  window.WorkbenchApp = {
    search: async function (keyword) {
      await loadCatalog(keyword);
      renderModuleCatalog();
    },
    refresh: async function () {
      await loadCatalog();
      await loadOnlinePlayers();
      renderModuleCatalog();
    },
    selectRequest: function (msgId) {
      if (!state.currentModule) return;
      const req = state.currentModule.requests.find(r => r.msgId === msgId);
      if (req) {
        state.currentRequest = req;
        state.currentPayload = {};
        renderModuleWorkbench(state.currentModule.moduleId);
      }
    },
    onPlayerChange: function (val) {
      state.selectedPlayerId = parseInt(val, 10) || 0;
    },
    quickTestLogin: function () {
      let modal = document.getElementById('wbTestLoginModal');
      if (!modal) {
        modal = document.createElement('div');
        modal.id = 'wbTestLoginModal';
        modal.style.cssText = 'position:fixed;top:0;left:0;right:0;bottom:0;background:rgba(0,0,0,0.7);display:flex;align-items:center;justify-content:center;z-index:9999;';
        modal.innerHTML = `
          <div style="background:var(--card-bg, #1e293b); border:1px solid var(--border-color, #334155); border-radius:8px; width:440px; max-width:90vw; padding:24px; box-shadow:0 10px 25px rgba(0,0,0,0.5);">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
              <h3 style="margin:0; font-size:1.15rem; color:var(--primary, #38bdf8); display:flex; align-items:center; gap:8px;">
                <span>🎮</span> 测试登录通道 (DEV/TEST)
              </h3>
              <button id="wbLoginModalClose" style="background:none; border:none; color:var(--text-muted, #94a3b8); font-size:1.2rem; cursor:pointer;">✕</button>
            </div>
            
            <div style="display:flex; gap:16px; margin-bottom:16px; border-bottom:1px solid var(--border-color, #334155); padding-bottom:8px;">
              <label id="wbRadioLabelAccount" style="display:flex; align-items:center; gap:6px; cursor:pointer; font-weight:600; color:#38bdf8;">
                <input type="radio" name="wbLoginMode" value="account" checked onchange="window.WorkbenchApp.switchLoginMode('account')">
                <span>账号名登录 (推荐)</span>
              </label>
              <label id="wbRadioLabelPlayerId" style="display:flex; align-items:center; gap:6px; cursor:pointer; font-weight:600; color:var(--text-muted, #94a3b8);">
                <input type="radio" name="wbLoginMode" value="playerId" onchange="window.WorkbenchApp.switchLoginMode('playerId')">
                <span>玩家ID登录</span>
              </label>
            </div>

            <div id="wbAccountLoginField" style="margin-bottom:16px;">
              <label style="display:block; font-size:0.85rem; color:var(--text-muted, #94a3b8); margin-bottom:6px;">账号名 / Account ID:</label>
              <input type="text" id="wbAccountInput" placeholder="例如: test_user_01 / admin" style="width:100%; padding:8px 12px; border-radius:4px; border:1px solid var(--border-color, #334155); background:#0f172a; color:#fff; font-size:0.9rem;" value="tester_01">
              <div style="font-size:0.75rem; color:#94a3b8; margin-top:4px;">输入任意账号名，系统自动检索已有角色或快速创建新测试角色</div>
            </div>

            <div id="wbPlayerIdLoginField" style="margin-bottom:16px; display:none;">
              <label style="display:block; font-size:0.85rem; color:var(--text-muted, #94a3b8); margin-bottom:6px;">玩家 ID (数值):</label>
              <input type="number" id="wbPlayerIdInput" placeholder="例如: 10001 (留空或0自动分配)" style="width:100%; padding:8px 12px; border-radius:4px; border:1px solid var(--border-color, #334155); background:#0f172a; color:#fff; font-size:0.9rem;" value="">
              <div style="font-size:0.75rem; color:#94a3b8; margin-top:4px;">输入指定数值 playerId，若已在线则直接激活，未激活自动就绪</div>
            </div>

            <div id="wbLoginTip" style="display:none; padding:8px 12px; border-radius:4px; font-size:0.85rem; margin-bottom:16px;"></div>

            <div style="display:flex; justify-content:flex-end; gap:10px;">
              <button class="btn btn-secondary" onclick="window.WorkbenchApp.closeLoginModal()">取消</button>
              <button id="wbLoginSubmitBtn" class="btn btn-primary" onclick="window.WorkbenchApp.doSubmitLogin()">立即登录</button>
            </div>
          </div>
        `;
        document.body.appendChild(modal);
        document.getElementById('wbLoginModalClose').onclick = () => window.WorkbenchApp.closeLoginModal();
        modal.onclick = (e) => {
          if (e.target === modal) window.WorkbenchApp.closeLoginModal();
        };
      }
      modal.style.display = 'flex';
      const tip = document.getElementById('wbLoginTip');
      if (tip) tip.style.display = 'none';
      const accountInput = document.getElementById('wbAccountInput');
      if (accountInput) accountInput.focus();
    },

    switchLoginMode: function (mode) {
      const accField = document.getElementById('wbAccountLoginField');
      const pidField = document.getElementById('wbPlayerIdLoginField');
      const lblAcc = document.getElementById('wbRadioLabelAccount');
      const lblPid = document.getElementById('wbRadioLabelPlayerId');
      if (mode === 'account') {
        if (lblAcc) lblAcc.style.color = '#38bdf8';
        if (lblPid) lblPid.style.color = 'var(--text-muted, #94a3b8)';
        if (accField) accField.style.display = 'block';
        if (pidField) pidField.style.display = 'none';
        const inp = document.getElementById('wbAccountInput');
        if (inp) inp.focus();
      } else {
        if (lblAcc) lblAcc.style.color = 'var(--text-muted, #94a3b8)';
        if (lblPid) lblPid.style.color = '#38bdf8';
        if (accField) accField.style.display = 'none';
        if (pidField) pidField.style.display = 'block';
        const inp = document.getElementById('wbPlayerIdInput');
        if (inp) inp.focus();
      }
    },

    closeLoginModal: function () {
      const modal = document.getElementById('wbTestLoginModal');
      if (modal) modal.style.display = 'none';
    },

    doSubmitLogin: async function () {
      const submitBtn = document.getElementById('wbLoginSubmitBtn');
      const tip = document.getElementById('wbLoginTip');
      const checkedMode = document.querySelector('input[name="wbLoginMode"]:checked')?.value || 'account';

      const payload = { timeoutMs: 5000 };
      if (checkedMode === 'account') {
        const acc = document.getElementById('wbAccountInput')?.value?.trim();
        if (!acc) {
          if (tip) {
            tip.style.display = 'block';
            tip.style.background = '#450a0a';
            tip.style.color = '#fca5a5';
            tip.innerText = '请输入有效的账号名';
          }
          return;
        }
        payload.accountId = acc;
      } else {
        const pidStr = document.getElementById('wbPlayerIdInput')?.value?.trim();
        const pid = parseInt(pidStr, 10) || 0;
        payload.playerId = pid;
      }

      try {
        if (submitBtn) {
          submitBtn.disabled = true;
          submitBtn.innerText = '登录中...';
        }
        if (tip) tip.style.display = 'none';

        const res = await apiFetch('/gm-console/api/workbench/test-login', 'POST', payload);
        if (res && res.ok && res.data) {
          window.WorkbenchApp.closeLoginModal();
          alert(`测试登录成功!\n账号: ${res.data.accountId || '无'}\n玩家ID: ${res.data.playerId}\n名称: ${res.data.name || '未命名'}\n状态: ${res.data.reused ? '复用已有角色' : '新创建测试角色'}`);
          await loadOnlinePlayers();
          state.selectedPlayerId = res.data.playerId;
          if (state.currentModule) {
            renderModuleWorkbench(state.currentModule.moduleId);
          }
        } else {
          if (tip) {
            tip.style.display = 'block';
            tip.style.background = '#450a0a';
            tip.style.color = '#fca5a5';
            tip.innerText = `测试登录失败: ${res?.summary || res?.message || '未知错误'}`;
          } else {
            alert(`测试登录失败: ${res?.summary || res?.message || '未知错误'}`);
          }
        }
      } catch (e) {
        if (tip) {
          tip.style.display = 'block';
          tip.style.background = '#450a0a';
          tip.style.color = '#fca5a5';
          tip.innerText = `测试登录异常: ${e.message}`;
        } else {
          alert(`测试登录请求异常: ${e.message}`);
        }
      } finally {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.innerText = '立即登录';
        }
      }
    },
    switchSubTab: function (tab) {
      if (state.activeSubTab === 'json' && tab === 'form') {
        const ta = document.getElementById('wbJsonTextarea');
        if (ta) {
          try {
            state.currentPayload = JSON.parse(ta.value || '{}');
          } catch (e) {
            alert('当前 JSON 格式非法，请先修正再切换回表单: ' + e.message);
            return;
          }
        }
      }
      state.activeSubTab = tab;
      if (tab === 'form' && state.currentModule) {
        renderModuleWorkbench(state.currentModule.moduleId);
        return;
      }
      const formEl = document.getElementById('wbFormContainer');
      const jsonEl = document.getElementById('wbJsonContainer');
      if (formEl && jsonEl) {
        formEl.style.display = tab === 'form' ? 'block' : 'none';
        jsonEl.style.display = tab === 'json' ? 'block' : 'none';
      }
      if (tab === 'json') {
        const ta = document.getElementById('wbJsonTextarea');
        if (ta) ta.value = JSON.stringify(state.currentPayload, null, 2);
      }
    },
    switchResultTab: function (tab) {
      state.activeResultTab = tab;
      if (state.currentModule) {
        renderModuleWorkbench(state.currentModule.moduleId);
      }
    },
    updateField: function (path, value) {
      if (value === undefined) {
        return;
      }
      setValueAtPath(state.currentPayload, path, value);
    },
    updateJsonField: function (path, rawJson) {
      try {
        const parsed = JSON.parse(rawJson || '{}');
        setValueAtPath(state.currentPayload, path, parsed);
      } catch (e) {
        alert(`字段 ${path} 的 JSON 格式非法: ` + e.message);
      }
    },
    addRepeatedItem: function (fieldPath, javaType) {
      let arr = getValueAtPath(state.currentPayload, fieldPath);
      if (!Array.isArray(arr)) {
        arr = [];
        setValueAtPath(state.currentPayload, fieldPath, arr);
      }
      if (javaType === 'MESSAGE') {
        arr.push({});
      } else if (javaType === 'INT' || javaType === 'FLOAT' || javaType === 'DOUBLE') {
        arr.push(0);
      } else if (javaType === 'BOOLEAN') {
        arr.push(false);
      } else {
        arr.push('');
      }
      if (state.currentModule) {
        renderModuleWorkbench(state.currentModule.moduleId);
      }
    },
    removeRepeatedItem: function (fieldPath, idx) {
      const arr = getValueAtPath(state.currentPayload, fieldPath);
      if (Array.isArray(arr) && idx >= 0 && idx < arr.length) {
        arr.splice(idx, 1);
      }
      if (state.currentModule) {
        renderModuleWorkbench(state.currentModule.moduleId);
      }
    },
    onJsonBlur: function (jsonStr) {
      try {
        state.currentPayload = JSON.parse(jsonStr || '{}');
      } catch (e) {
        alert('输入的 JSON 格式非法: ' + e.message);
      }
    },
    resetForm: function () {
      state.currentPayload = {};
      if (state.currentModule) {
        renderModuleWorkbench(state.currentModule.moduleId);
      }
    },
    invoke: async function () {
      if (!state.currentRequest) return;
      if (state.activeSubTab === 'json') {
        const ta = document.getElementById('wbJsonTextarea');
        if (ta) {
          try {
            state.currentPayload = JSON.parse(ta.value || '{}');
          } catch (e) {
            alert('输入的 JSON 格式非法: ' + e.message);
            return;
          }
        }
      }
      if (state.selectedPlayerId <= 0) {
        alert('请先在右上角选择调试玩家或点击「测试登录」创建测试角色！');
        return;
      }
      const reqBody = {
        playerId: state.selectedPlayerId,
        msgId: state.currentRequest.msgId,
        msgName: state.currentRequest.msgName,
        payload: state.currentPayload,
        withTrace: state.withTrace,
        withDiff: state.withDiff,
        mirrorToRealClient: state.mirrorToRealClient,
        timeoutMs: state.timeoutMs
      };

      try {
        const res = await apiFetch('/gm-console/api/proto/invoke', 'POST', reqBody);
        state.lastResult = res;
        const el = document.getElementById('wbResultContent');
        if (el) el.innerHTML = renderResultTabContent();
      } catch (e) {
        alert(`协议模拟请求异常: ${e.message}`);
      }
    },
    saveTemplate: function () {
      if (!state.currentRequest) return;
      const tName = prompt('输入模板名称:', `${state.currentRequest.msgName}_默认`);
      if (!tName) return;
      const key = `wb_tpl_${state.currentRequest.msgId}_${tName}`;
      localStorage.setItem(key, JSON.stringify(state.currentPayload));
      alert(`模板「${tName}」已成功保存至本地！`);
    },
    loadTemplate: function () {
      if (!state.currentRequest) return;
      const prefix = `wb_tpl_${state.currentRequest.msgId}_`;
      const keys = [];
      for (let i = 0; i < localStorage.length; i++) {
        const k = localStorage.key(i);
        if (k.startsWith(prefix)) keys.push(k.substring(prefix.length));
      }
      if (keys.length === 0) {
        alert('未找到该协议的已保存模板');
        return;
      }
      const chosen = prompt(`已保存模板列表:\n${keys.join('\n')}\n请输入要载入的模板名称:`, keys[0]);
      if (!chosen) return;
      const raw = localStorage.getItem(prefix + chosen);
      if (raw) {
        try {
          state.currentPayload = JSON.parse(raw);
          renderModuleWorkbench(state.currentModule.moduleId);
        } catch (e) {
          alert('解析模板失败: ' + e.message);
        }
      }
    },
    copyCurl: function () {
      if (!state.currentRequest) return;
      const body = {
        playerId: state.selectedPlayerId || 10001,
        msgId: state.currentRequest.msgId,
        payload: state.currentPayload,
        withTrace: state.withTrace,
        withDiff: state.withDiff
      };
      const baseOrigin = (window.location && window.location.origin && window.location.origin !== 'null')
        ? window.location.origin
        : 'http://127.0.0.1:17010';
      const curl = `curl -X POST "${baseOrigin}/gm-console/api/proto/invoke" \\\n  -H "Content-Type: application/json" \\\n  -d '${JSON.stringify(body, null, 2)}'`;
      navigator.clipboard.writeText(curl).then(() => alert('cURL 命令已复制到剪贴板！')).catch(() => prompt('请手动复制 cURL:', curl));
    },
    copyJavaSnippet: function () {
      if (!state.currentRequest) return;
      const payloadLiteral = JSON.stringify(state.currentPayload || {});
      const java = `// WhiteBoxTestKit 协议模拟测试片段 (${state.currentRequest.msgName})\nProtoInvokeRequest req = new ProtoInvokeRequest();\nreq.setPlayerId(${state.selectedPlayerId || 10001}L);\nreq.setMsgId(${state.currentRequest.msgId});\n// payload: ${payloadLiteral}\nMap<String, Object> result = ProtocolSimulationService.getInstance().invoke(req);\nAssertions.assertTrue((boolean) result.get("ok"));`;
      navigator.clipboard.writeText(java).then(() => alert('Java 测试片段已复制到剪贴板！')).catch(() => prompt('请手动复制 Java 片段:', java));
    },
    fillGmAction: function (action) {
      if (window.switchTab) window.switchTab('tabSchema');
      const selAction = document.getElementById('selAction');
      if (selAction) {
        selAction.value = action;
        if (window.onActionChange) window.onActionChange();
      }
    }
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initWorkbench);
  } else {
    initWorkbench();
  }
})();