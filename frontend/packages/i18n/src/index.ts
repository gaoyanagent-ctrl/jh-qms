import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

export const resources = {
  'zh-CN': {
    translation: {
      app: {
        name: 'IAF 平台',
        workbench: '工作台',
        logout: '退出登录'
      },
      common: {
        actions: {
          create: '新增',
          edit: '编辑',
          disable: '禁用',
          resetPassword: '重置密码',
          save: '保存',
          confirm: '确定',
          cancel: '取消',
          reset: '重置',
          search: '搜索',
          assign: '分配',
          refresh: '刷新',
          view: '查看',
          retry: '重试',
          back: '返回',
          actions: '操作'
        },
        yes: '是',
        no: '否',
        notAvailable: '—',
        validation: {
          required: '请填写此字段',
          maxLength: '最多输入 {{max}} 个字符'
        },
        fields: {
          actions: '操作',
          status: '状态',
          createdAt: '创建时间',
          updatedAt: '更新时间'
        },
        status: {
          ACTIVE: '启用',
          ENABLED: '启用',
          DISABLED: '禁用'
        },
        feedback: {
          empty: '暂无数据',
          loadFailed: '加载失败',
          loading: '加载中',
          operationSucceeded: '操作成功',
          confirmDisable: '确认禁用该记录？'
        }
      },
      auth: {
        loginTitle: '平台登录',
        loginSubtitle: '使用企业身份进入平台工作区',
        brandTitle: '面向制造企业的工业应用基础平台',
        brandSubtitle: '统一平台管理、权限、组织、审批、集成和业务扩展能力，为 WMS、MES、SRM、QMS 等工业系统提供一致工程底座。',
        opsTitle: '工业运营视图',
        opsDescription: '主题、权限和复杂视图能力从平台层统一治理，支撑管理端、Kanban、设计器和大屏场景。',
        telemetry: {
          availability: {
            label: '平台可用性',
            value: '99.9%'
          },
          throughput: {
            label: '流程处理',
            value: '24/7'
          },
          exceptions: {
            label: '异常闭环',
            value: '实时'
          }
        },
        tenantCode: '租户编码',
        tenantCodePlaceholder: '请输入租户编码',
        username: '用户名',
        password: '密码',
        submit: '登录',
        required: '请输入必填项',
        failed: '登录失败',
        sso: {
          dingtalk: '钉钉',
          wecom: '企业微信',
          google: 'Google'
        },
        loginTemplates: {
          brandShort: 'IAF',
          welcomeBack: '欢迎回来',
          ssoTitle: '其他登录方式',
          rememberMe: '记住我 30 天',
          forgotPassword: '忘记密码？',
          passwordPlaceholder: '请输入密码',
          copyright: '© 2026 IAF Framework. All rights reserved.',
          standardIndustrial: {
            name: '标准工业',
            subtitle: '请登录您的 IAF 开发者账号',
            usernamePlaceholder: '请输入员工号/邮箱',
            submit: '登录系统'
          },
          cyberAi: {
            name: 'Cyber AI',
            title: 'IDENTITY VERIFICATION',
            subtitle: '请输入您的中控身份凭证',
            systemName: 'IAF_SYSTEM',
            kicker: 'IAF_SYSTEM // SECURE_ACCESS',
            username: 'Developer ID',
            usernamePlaceholder: 'DEV-****',
            password: 'Access Token',
            submit: 'Authenticate',
            ssoTitle: 'Alternative Access',
            identity: '身份校验链路已就绪',
            permission: '权限上下文等待载入',
            workflow: '审批与流程内核在线',
            footer: '身份、权限和租户上下文将在登录后统一初始化。'
          },
          immersiveGlass: {
            name: '沉浸驾驶舱',
            cockpit: 'IAF COCKPIT',
            hub: 'Industrial System Hub',
            subtitle: '请输入您的中控身份凭证',
            username: 'Operator ID',
            usernamePlaceholder: 'OP-984',
            password: 'Access Key',
            submit: '验证并登录',
            ssoTitle: '第三方协同登录',
            pipeline: 'LIVE PIPELINE MONITOR',
            features: {
              platform: '统一基础平台',
              model: '通用制造模型',
              dsl: '轻量 DSL 编译'
            }
          },
          minimalTechnical: {
            name: '极简技术',
            title: '身份凭证授权',
            subtitle: 'INPUT AUTHORIZATION CODES TO LOGIN',
            username: 'UID // 用户工号',
            usernamePlaceholder: 'EMP-8291-IAF',
            password: 'KEY // 校验密码',
            forgot: 'Forgot Key?',
            submit: 'INITIALIZE ACCESS',
            ssoTitle: 'LDAP / SSO INTEGRATION',
            loc: 'SYS.LOC // LAT: 31.2304° N, LON: 121.4737° E',
            version: 'IAF FRAMEWORK V2.4 // AGENT_COMPILED',
            specLabel: 'SYSTEM SPECIFICATION',
            accessLabel: 'SECURE ACCESS',
            description: '面向制造企业的 AI Coding 应用开发底座。专为极端精确、统一架构、状态机、多流转、轻量 DSL 及源码自主生成而设计。',
            blocks: {
              infra: {
                title: 'UNIFIED INFRASTRUCTURE / 基础平台',
                description: '整合 WMS、MES、SRM、QMS 的通用底座架构，提供高级别并发一致性控制。'
              },
              source: {
                title: 'AI SOURCE SYNTHESIZER / 源码生成',
                description: '轻量级工控 DSL 模型，全链路源码解析生成，保障企业代码可溯源、可自主管控。'
              }
            }
          },
          bento: {
            name: 'Bento 工作台',
            title: '登录',
            subtitle: '工业应用开发框架',
            description: '进入 AI Coding 应用开发底座',
            username: '工作邮箱 / 账号',
            usernamePlaceholder: 'admin@company.com',
            submit: '立即登录',
            ssoTitle: '第三方登录',
            contactAdmin: '还没有账号？联系管理员分配',
            poweredBy: 'Powered by Generative AI',
            features: {
              model: {
                title: '通用模型',
                description: '组织、用户、角色、权限'
              },
              document: {
                title: '业务单据',
                description: '状态、审批、审计'
              },
              ai: {
                title: 'AI Coding',
                description: '规则、任务、代码地图'
              },
              code: {
                title: '工程治理',
                description: '模块、测试、质量门禁'
              },
              workflow: {
                title: '流程协同',
                description: 'Kanban 与审批中心'
              },
              integration: {
                title: '企业集成',
                description: '消息、通讯录、身份'
              }
            }
          }
        }
      },
      menu: {
        workbench: '工作台',
        platform: '平台管理',
        users: '用户管理',
        orgs: '组织管理',
        roles: '角色权限',
        menus: '菜单权限',
        dictionaries: '字典参数',
        auditLogs: '操作日志',
        approvalTasks: '审批任务',
        kanban: '平台 Kanban',
        qms: '质量管理',
        qmsParts: '工程数据',
        qmsPartDetail: '零件详情'
      },
      qms: {
        status: {
          ACTIVE: '启用', INACTIVE: '停用', OBSOLETE: '作废', DRAFT: '草稿', UPLOADED: '已上传',
          PARSING: '解析中', PARSED: '已解析', REVIEWING: '复核中', CONFIRMED: '已确认',
          RELEASED: '已发布', SUPERSEDED: '已替代', FAILED: '失败', PENDING: '待处理',
          RUNNING: '处理中', PARTIAL_SUCCESS: '部分成功', SUCCESS: '成功', CANCELLED: '已取消', REJECTED: '已驳回'
        },
        drawingType: { PRODUCT: '产品图', PART: '零件图', ASSEMBLY: '装配图', OTHER: '其他' },
        sourceSystem: { MANUAL: '手工维护', PLM: 'PLM', MIGRATION: '历史迁移' },
        feedback: {
          loadPartsFailed: '无法加载零件清单，请重试。',
          loadPartDetailFailed: '无法加载零件及图纸信息，请重试。',
          loadRevisionsFailed: '无法加载版本历史。',
          createPartFailed: '零件创建失败，请检查输入后重试。',
          createDrawingFailed: '图纸创建失败，请检查输入后重试。',
          createRevisionFailed: '版本创建失败，请检查输入后重试。',
          invalidPartId: '零件地址无效，请返回工程数据清单。',
          drawingViewRestricted: '当前账号无权查看图纸数据。',
          revisionViewRestricted: '当前账号无权查看版本历史。'
        }
      },
      qmsParts: {
        title: '工程数据',
        description: '维护当前组织的零件主数据，并进入图纸与版本层级。',
        search: { keyword: '关键词', placeholder: '搜索零件号、名称、物料号或车型' },
        fields: {
          partNo: '零件号', partName: '零件名称', materialNo: '物料号', vehicleModel: '车型',
          importanceLevel: '重要度', customerId: '客户 ID', supplierId: '供应商 ID', orgId: '组织 ID', version: '数据版本'
        }
      },
      qmsPartDetail: {
        title: '零件详情'
      },
      qmsDrawings: {
        title: '图纸', empty: '当前零件暂无图纸', actions: { create: '新增图纸' },
        fields: { drawingNo: '图号', drawingName: '图纸名称', drawingType: '图纸类型', sourceSystem: '来源系统' }
      },
      qmsRevisions: {
        title: '版本历史', empty: '当前图纸暂无版本', selectDrawing: '选择一张图纸以查看版本历史。', actions: { create: '新增版本', upload: '上传文件' },
        feedback: { uploadSucceeded: '图纸文件上传成功', uploadPermissionRequired: '当前登录身份缺少上传权限，请重新登录或联系管理员。' },
        fields: {
          revisionCode: '版本号', revisionSeq: '版本序号', effectiveDate: '生效日期', parseStatus: '解析状态',
          reviewStatus: '复核状态', supersedesRevision: '替代版本', file: '源文件'
        }
      },
      settings: {
        theme: '主题',
        formInteractionMode: '新增编辑模式',
        density: '界面密度',
        fontSize: '字体大小',
        sidebarMode: '侧边栏模式',
        sidebarCollapsed: '默认折叠侧栏',
        sidebarWidth: '侧栏宽度',
        motionLevel: '动效级别',
        surfaceWidth: '表单宽度',
        workspaceMode: '工作模式',
        themes: {
          lightIndustrial: '浅色工业',
          darkIndustrial: '暗色工业',
          compactIndustrial: '紧凑工业',
          dashboardIndustrial: '大屏工业',
          mobileWork: '移动作业',
          highContrast: '高对比',
          customerBrand: '客户品牌'
        },
        formInteractionModes: {
          modal: '弹窗',
          drawer: '抽屉',
          page: '页面'
        },
        densities: {
          compact: '紧凑',
          standard: '标准',
          comfortable: '舒适'
        },
        fontSizes: {
          small: '小',
          default: '默认',
          large: '大'
        },
        sidebarModes: {
          dark: '深色',
          light: '浅色'
        },
        motionLevels: {
          none: '关闭',
          subtle: '轻量',
          standard: '标准'
        },
        surfaceWidths: {
          standard: '标准',
          wide: '宽',
          extraWide: '超宽'
        },
        workspaceModes: {
          simple: '简洁模式',
          expert: '专家模式'
        }
      },
      profile: {
        preferencesTitle: '个人偏好',
        savePreferences: '保存偏好',
        resetPreferences: '恢复默认',
        preferencesSaved: '个人偏好已保存',
        preferencesSavedLocally: '后端偏好服务不可用，已保存到本地',
        preferencesReset: '个人偏好已恢复默认',
        preferencesResetLocally: '后端偏好服务不可用，已在本地恢复默认',
        preferenceDescription: '偏好优先保存在后端用户档案中，本地浏览器配置作为离线和开发环境 fallback。',
        sections: {
          appearance: '视觉外观',
          workspace: '工作方式'
        }
      },
      shell: {
        brandSubtitle: '工业应用框架',
        workspace: '平台工作区',
        tenant: '租户 {{id}}',
        tenantFallback: '租户',
        menuSearch: '搜索菜单',
        noMenuResults: '未找到菜单',
        globalSearch: '全局搜索',
        notifications: '通知',
        language: '语言',
        collapseSidebar: '折叠侧边栏',
        expandSidebar: '展开侧边栏',
        resizeSidebar: '拖拽调整侧边栏宽度',
        profile: '个人中心',
        preferences: '偏好设置',
        commandPlaceholder: '搜索菜单、路由或功能',
        languageSwitched: '语言已切换'
      },
      notifications: {
        approval: {
          title: '审批任务待处理',
          description: '有新的平台审批任务进入待办，请及时处理。'
        },
        integration: {
          title: '企业集成同步异常',
          description: '企业微信同步出现重试记录，建议检查集成配置。'
        },
        kanban: {
          title: 'Kanban 卡片已更新',
          description: '平台 Kanban 有卡片完成了状态流转。'
        }
      },
      workbench: {
        title: '平台工作台',
        description: '平台基础能力管理入口',
        platformFoundation: '平台基础能力',
        openKanban: '打开 Kanban',
        kanbanDescription: '用于验证平台任务、审批、异常和调度类复杂视图的第一版样板。',
        today: '今日',
        open: '打开',
        operationFocus: '运营焦点',
        systemHealth: '系统健康',
        quickAccess: '快捷入口',
        metrics: {
          pendingApprovals: '待办审批',
          workflowHealth: '流程健康',
          integrationAlerts: '集成告警',
          permissionChanges: '权限变更'
        },
        metricHints: {
          pendingApprovals: '需要当天处理',
          workflowHealth: '近 24 小时成功率',
          integrationAlerts: '等待运维确认',
          permissionChanges: '近 7 天变更'
        },
        operations: {
          approval: {
            title: '采购额度审批即将超时',
            description: '高优先级审批任务需要负责人确认，避免影响采购执行。'
          },
          exception: {
            title: '现场异常待分派',
            description: '异常任务已进入平台 Kanban，等待调度人员处理。'
          },
          sync: {
            title: '企业集成同步失败',
            description: '企业微信同步出现失败记录，建议检查重试策略和凭据。'
          }
        },
        health: {
          auth: '认证与权限',
          workflow: '审批流程',
          integration: '企业集成'
        },
        quickLinks: {
          users: '用户管理',
          roles: '角色权限',
          orgs: '组织管理',
          kanban: '平台 Kanban'
        }
      },
      kanban: {
        title: '平台 Kanban',
        filterKeyword: '搜索卡片',
        wipNormal: 'WIP 正常',
        wipOverLimit: 'WIP 超限',
        emptyColumn: '暂无卡片',
        views: {
          operations: '运营看板'
        },
        columns: {
          pending: '待处理',
          processing: '处理中',
          done: '已完成'
        },
        cards: {
          approval: '审批任务待处理',
          exception: '现场异常待分派',
          workflow: '流程定义评审',
          sync: '企业集成同步'
        },
        priority: {
          ALL: '全部优先级',
          HIGH: '高',
          MEDIUM: '中',
          LOW: '低'
        },
        sources: {
          approval: '审批',
          exception: '异常',
          workflow: '流程',
          sync: '集成'
        },
        detail: {
          owner: '负责人',
          source: '来源',
          nextAction: '下一步'
        },
        detailActions: {
          approval: '确认审批资料、检查字段权限后执行同意、拒绝或退回。',
          exception: '确认异常责任人和处理时限，然后拖动到处理中。',
          workflow: '完成流程定义评审后移动到已完成。',
          sync: '检查同步日志和凭据，确认重试结果。'
        }
      },
      users: {
        title: '用户管理',
        username: '用户名',
        displayName: '显示名',
        mobile: '手机号',
        email: '邮箱',
        primaryOrgId: '主组织 ID',
        organizations: '组织归属',
        primaryOrganization: '主组织',
        assignOrganizations: '分配组织',
        primaryOrgMustBeAssigned: '主组织必须在组织归属中',
        password: '初始密码',
        newPassword: '新密码'
      },
      orgs: {
        title: '组织管理',
        orgCode: '组织编码',
        orgName: '组织名称',
        orgType: '组织类型',
        parentId: '父组织 ID',
        sortNo: '排序',
        type: {
          COMPANY: '公司',
          DEPARTMENT: '部门',
          TEAM: '小组'
        }
      },
      roles: {
        title: '角色权限',
        roleCode: '角色编码',
        roleName: '角色名称',
        roleType: '角色类型',
        permissions: '权限',
        assignPermissions: '分配权限',
        assignMenus: '分配菜单'
      },
      workspace: {
        refresh: '刷新',
        close: '关闭',
        closeOthers: '关闭其他',
        closeRight: '关闭右侧',
        pin: '固定',
        unpin: '取消固定',
        columnSettings: '列设置',
        moveColumnUp: '上移列',
        moveColumnDown: '下移列',
        listReady: '列表就绪',
        totalRecords: '共 {{total}} 条',
        visibleColumns: '{{count}} 列可见',
        selectedRows: '已选 {{count}} 行',
        activeFilters: '{{count}} 个筛选',
        standardListView: '标准列表视图',
        confirmCloseTitle: '有未保存的更改',
        confirmCloseContent: '关闭此标签页将丢失未保存的更改。确定要关闭吗？',
        expertMode: '专家模式'
      },
      permissions: {
        groups: {
          auth: '认证',
          user: '用户',
          org: '组织',
          role: '角色',
          menu: '菜单',
          permission: '权限点',
          platform: '平台',
          platformConfig: '平台配置'
        },
        platform: {
          authMe: '查看当前用户',
          userView: '查看用户',
          userCreate: '新增用户',
          userUpdate: '编辑用户',
          userDisable: '禁用用户',
          userResetPassword: '重置用户密码',
          orgView: '查看组织',
          orgCreate: '新增组织',
          orgUpdate: '编辑组织',
          roleView: '查看角色',
          roleCreate: '新增角色',
          roleUpdate: '编辑角色',
          roleAssignPermission: '分配角色权限',
          roleAssignMenu: '分配角色菜单',
          menuView: '查看菜单',
          menuCreate: '新增菜单',
          menuUpdate: '编辑菜单',
          menuDisable: '禁用菜单',
          permissionView: '查看权限点',
          dataPermissionView: '查看数据权限',
          dataPermissionUpdate: '维护数据权限',
          fieldPermissionView: '查看字段权限',
          fieldPermissionUpdate: '维护字段权限',
          dictionaryView: '查看字典',
          dictionaryUpdate: '维护字典',
          parameterView: '查看参数',
          parameterUpdate: '维护参数',
          auditView: '查看审计日志',
          themeView: '查看主题配置',
          themeUpdate: '维护主题配置',
          brandView: '查看品牌配置',
          brandUpdate: '维护品牌配置',
          i18nView: '查看多语言资源',
          i18nUpdate: '维护多语言资源',
          preferenceMe: '维护个人偏好'
        }
      },
      platformConfig: {
        mockFirst: '前端样例',
        menuConsole: '菜单权限控制台',
        menuSummary: '菜单与权限映射',
        dictionaryParameter: '字典与参数',
        dictionary: '字典',
        parameter: '参数',
        auditLog: '操作日志',
        menuTitle: '菜单名称',
        menuCode: '菜单编码',
        menuType: '菜单类型',
        parentMenu: '上级菜单',
        titleKey: '标题 Key',
        routePath: '路由',
        componentKey: '组件 Key',
        icon: '图标',
        sortNo: '排序',
        visible: '可见',
        enabled: '启用',
        hiddenMenu: '隐藏',
        permissionCode: '权限码',
        dictType: '字典类型',
        dictCode: '字典编码',
        dictName: '字典名称',
        parameterGroup: '参数分组',
        parameterKey: '参数键',
        parameterValue: '参数值',
        parameterScope: '作用域',
        operator: '操作人',
        module: '模块',
        action: '动作',
        result: '结果',
        time: '时间',
        auditSummary: '今日操作记录',
        results: {
          SUCCESS: '成功',
          FAILED: '失败'
        }
      },
      approval: {
        title: '审批任务中心',
        listTitle: '审批任务列表',
        listDescription: '按待办、已办、我发起的视角查看平台审批任务。',
        todoCount: '待办 {{count}}',
        documentNo: '单据号',
        taskTitle: '任务标题',
        requester: '发起人',
        priority: '优先级',
        fieldPermission: '字段权限',
        fieldPermissionDescription: '当前审批视图按字段权限展示，敏感字段在后端接口接入后将只读或隐藏。',
        timeline: '审批时间线',
        tabs: {
          todo: '我的待办',
          done: '我的已办',
          started: '我发起的'
        },
        actions: {
          approve: '同意',
          reject: '拒绝',
          return: '退回'
        },
        feedback: {
          approved: '审批已同意',
          rejected: '审批已拒绝',
          returned: '审批已退回'
        },
        tasks: {
          purchaseLimit: '采购额度审批',
          roleChange: '角色权限变更审批',
          integration: '企业集成配置审批',
          parameter: '系统参数变更审批'
        },
        timelineItems: {
          started: '流程已发起',
          waiting: '等待当前节点处理'
        }
      }
    }
  },
  'en-US': {
    translation: {
      app: {
        name: 'IAF Platform',
        workbench: 'Workbench',
        logout: 'Logout'
      },
      common: {
        actions: {
          create: 'Create',
          edit: 'Edit',
          disable: 'Disable',
          resetPassword: 'Reset password',
          save: 'Save',
          confirm: 'Confirm',
          cancel: 'Cancel',
          reset: 'Reset',
          search: 'Search',
          assign: 'Assign',
          refresh: 'Refresh',
          view: 'View',
          retry: 'Retry',
          back: 'Back',
          actions: 'Actions'
        },
        yes: 'Yes',
        no: 'No',
        notAvailable: '—',
        validation: {
          required: 'This field is required',
          maxLength: 'Enter no more than {{max}} characters'
        },
        fields: {
          actions: 'Actions',
          status: 'Status',
          createdAt: 'Created at',
          updatedAt: 'Updated at'
        },
        status: {
          ACTIVE: 'Active',
          ENABLED: 'Enabled',
          DISABLED: 'Disabled'
        },
        feedback: {
          empty: 'No data',
          loadFailed: 'Load failed',
          loading: 'Loading',
          operationSucceeded: 'Operation succeeded',
          confirmDisable: 'Disable this record?'
        }
      },
      auth: {
        loginTitle: 'Platform login',
        loginSubtitle: 'Use enterprise identity to enter the platform workspace',
        brandTitle: 'Industrial application foundation for manufacturing enterprises',
        brandSubtitle: 'Unified platform management, permissions, organizations, approvals, integrations, and extensibility for WMS, MES, SRM, QMS, and related industrial systems.',
        opsTitle: 'Industrial operations view',
        opsDescription: 'Theme, permission, and complex-view capabilities are governed at the platform layer for admin, Kanban, designer, and dashboard scenarios.',
        telemetry: {
          availability: {
            label: 'Platform availability',
            value: '99.9%'
          },
          throughput: {
            label: 'Workflow processing',
            value: '24/7'
          },
          exceptions: {
            label: 'Exception closure',
            value: 'Realtime'
          }
        },
        tenantCode: 'Tenant code',
        tenantCodePlaceholder: 'Enter tenant code',
        username: 'Username',
        password: 'Password',
        submit: 'Login',
        required: 'This field is required',
        failed: 'Login failed',
        sso: {
          dingtalk: 'DingTalk',
          wecom: 'WeCom',
          google: 'Google'
        },
        loginTemplates: {
          brandShort: 'IAF',
          welcomeBack: 'Welcome back',
          ssoTitle: 'Alternative sign-in',
          rememberMe: 'Remember me for 30 days',
          forgotPassword: 'Forgot password?',
          passwordPlaceholder: 'Enter your password',
          copyright: '© 2026 IAF Framework. All rights reserved.',
          standardIndustrial: {
            name: 'Standard Industrial',
            subtitle: 'Sign in with your IAF developer account',
            usernamePlaceholder: 'Enter employee ID or email',
            submit: 'Sign in'
          },
          cyberAi: {
            name: 'Cyber AI',
            title: 'IDENTITY VERIFICATION',
            subtitle: 'Authenticate to access the framework',
            systemName: 'IAF_SYSTEM',
            kicker: 'IAF_SYSTEM // SECURE_ACCESS',
            username: 'Developer ID',
            usernamePlaceholder: 'DEV-****',
            password: 'Access Token',
            submit: 'Authenticate',
            ssoTitle: 'Alternative Access',
            identity: 'Identity validation chain is ready',
            permission: 'Permission context is waiting to load',
            workflow: 'Approval and workflow kernel is online',
            footer: 'Identity, permission, and tenant context are initialized after login.'
          },
          immersiveGlass: {
            name: 'Immersive Cockpit',
            cockpit: 'IAF COCKPIT',
            hub: 'Industrial System Hub',
            subtitle: 'Enter your control-center credentials',
            username: 'Operator ID',
            usernamePlaceholder: 'OP-984',
            password: 'Access Key',
            submit: 'Verify and sign in',
            ssoTitle: 'Federated access',
            pipeline: 'LIVE PIPELINE MONITOR',
            features: {
              platform: 'Unified platform base',
              model: 'Common manufacturing model',
              dsl: 'Lightweight DSL compiler'
            }
          },
          minimalTechnical: {
            name: 'Minimal Technical',
            title: 'Credential Authorization',
            subtitle: 'INPUT AUTHORIZATION CODES TO LOGIN',
            username: 'UID // Employee ID',
            usernamePlaceholder: 'EMP-8291-IAF',
            password: 'KEY // Access secret',
            forgot: 'Forgot Key?',
            submit: 'INITIALIZE ACCESS',
            ssoTitle: 'LDAP / SSO INTEGRATION',
            loc: 'SYS.LOC // LAT: 31.2304° N, LON: 121.4737° E',
            version: 'IAF FRAMEWORK V2.4 // AGENT_COMPILED',
            specLabel: 'SYSTEM SPECIFICATION',
            accessLabel: 'SECURE ACCESS',
            description: 'An AI Coding application base for manufacturing enterprises, designed for exacting structure, unified architecture, state flows, lightweight DSL, and source-code generation.',
            blocks: {
              infra: {
                title: 'UNIFIED INFRASTRUCTURE / Platform Base',
                description: 'Combines the common foundation for WMS, MES, SRM, and QMS with strong consistency controls.'
              },
              source: {
                title: 'AI SOURCE SYNTHESIZER / Code Generation',
                description: 'Uses a lightweight industrial DSL and end-to-end code generation to keep enterprise code traceable and controllable.'
              }
            }
          },
          bento: {
            name: 'Bento Workspace',
            title: 'Sign in',
            subtitle: 'Industrial Application Framework',
            description: 'Enter the AI Coding application base',
            username: 'Work email / account',
            usernamePlaceholder: 'admin@company.com',
            submit: 'Sign in now',
            ssoTitle: 'Third-party sign-in',
            contactAdmin: 'No account yet? Contact your administrator.',
            poweredBy: 'Powered by Generative AI',
            features: {
              model: {
                title: 'Common Model',
                description: 'Organizations, users, roles, permissions'
              },
              document: {
                title: 'Business Document',
                description: 'Status, approval, audit'
              },
              ai: {
                title: 'AI Coding',
                description: 'Rules, tasks, code map'
              },
              code: {
                title: 'Engineering',
                description: 'Modules, tests, quality gate'
              },
              workflow: {
                title: 'Workflow',
                description: 'Kanban and approval center'
              },
              integration: {
                title: 'Integration',
                description: 'Messages, contacts, identity'
              }
            }
          }
        }
      },
      menu: {
        workbench: 'Workbench',
        platform: 'Platform',
        users: 'Users',
        orgs: 'Organizations',
        roles: 'Roles',
        menus: 'Menus & Permissions',
        dictionaries: 'Dictionaries & Parameters',
        auditLogs: 'Operation Logs',
        approvalTasks: 'Approval Tasks',
        kanban: 'Platform Kanban',
        qms: 'Quality Management',
        qmsParts: 'Engineering Data',
        qmsPartDetail: 'Part Detail'
      },
      qms: {
        status: {
          ACTIVE: 'Active', INACTIVE: 'Inactive', OBSOLETE: 'Obsolete', DRAFT: 'Draft', UPLOADED: 'Uploaded',
          PARSING: 'Parsing', PARSED: 'Parsed', REVIEWING: 'Reviewing', CONFIRMED: 'Confirmed',
          RELEASED: 'Released', SUPERSEDED: 'Superseded', FAILED: 'Failed', PENDING: 'Pending',
          RUNNING: 'Running', PARTIAL_SUCCESS: 'Partial success', SUCCESS: 'Success', CANCELLED: 'Cancelled', REJECTED: 'Rejected'
        },
        drawingType: { PRODUCT: 'Product', PART: 'Part', ASSEMBLY: 'Assembly', OTHER: 'Other' },
        sourceSystem: { MANUAL: 'Manual', PLM: 'PLM', MIGRATION: 'Migration' },
        feedback: {
          loadPartsFailed: 'Parts could not be loaded. Please retry.',
          loadPartDetailFailed: 'Part and drawing data could not be loaded. Please retry.',
          loadRevisionsFailed: 'Revision history could not be loaded.',
          createPartFailed: 'The part could not be created. Check the form and retry.',
          createDrawingFailed: 'The drawing could not be created. Check the form and retry.',
          createRevisionFailed: 'The revision could not be created. Check the form and retry.',
          invalidPartId: 'The part address is invalid. Return to Engineering Data.',
          drawingViewRestricted: 'Your account cannot view drawing data.',
          revisionViewRestricted: 'Your account cannot view revision history.'
        }
      },
      qmsParts: {
        title: 'Engineering Data',
        description: 'Maintain part master data for the current organization and navigate drawing revisions.',
        search: { keyword: 'Keyword', placeholder: 'Search part no., name, material no., or vehicle model' },
        fields: {
          partNo: 'Part no.', partName: 'Part name', materialNo: 'Material no.', vehicleModel: 'Vehicle model',
          importanceLevel: 'Importance', customerId: 'Customer ID', supplierId: 'Supplier ID', orgId: 'Organization ID', version: 'Data version'
        }
      },
      qmsPartDetail: {
        title: 'Part Detail'
      },
      qmsDrawings: {
        title: 'Drawings', empty: 'No drawings for this part', actions: { create: 'Create Drawing' },
        fields: { drawingNo: 'Drawing no.', drawingName: 'Drawing name', drawingType: 'Drawing type', sourceSystem: 'Source system' }
      },
      qmsRevisions: {
        title: 'Revision History', empty: 'No revisions for this drawing', selectDrawing: 'Select a drawing to view its revision history.', actions: { create: 'Create Revision', upload: 'Upload File' },
        feedback: { uploadSucceeded: 'Drawing file uploaded', uploadPermissionRequired: 'The current session lacks upload permission. Sign in again or contact an administrator.' },
        fields: {
          revisionCode: 'Revision', revisionSeq: 'Sequence', effectiveDate: 'Effective date', parseStatus: 'Parse status',
          reviewStatus: 'Review status', supersedesRevision: 'Supersedes revision', file: 'Source file'
        }
      },
      settings: {
        theme: 'Theme',
        formInteractionMode: 'Create/edit mode',
        density: 'Density',
        fontSize: 'Font size',
        sidebarMode: 'Sidebar mode',
        sidebarCollapsed: 'Collapse sidebar by default',
        sidebarWidth: 'Sidebar width',
        motionLevel: 'Motion',
        surfaceWidth: 'Form width',
        workspaceMode: 'Workspace mode',
        themes: {
          lightIndustrial: 'Light Industrial',
          darkIndustrial: 'Dark Industrial',
          compactIndustrial: 'Compact Industrial',
          dashboardIndustrial: 'Dashboard Industrial',
          mobileWork: 'Mobile Work',
          highContrast: 'High Contrast',
          customerBrand: 'Customer Brand'
        },
        formInteractionModes: {
          modal: 'Modal',
          drawer: 'Drawer',
          page: 'Page'
        },
        densities: {
          compact: 'Compact',
          standard: 'Standard',
          comfortable: 'Comfortable'
        },
        fontSizes: {
          small: 'Small',
          default: 'Default',
          large: 'Large'
        },
        sidebarModes: {
          dark: 'Dark',
          light: 'Light'
        },
        motionLevels: {
          none: 'Off',
          subtle: 'Subtle',
          standard: 'Standard'
        },
        surfaceWidths: {
          standard: 'Standard',
          wide: 'Wide',
          extraWide: 'Extra wide'
        },
        workspaceModes: {
          simple: 'Simple mode',
          expert: 'Expert mode'
        }
      },
      profile: {
        preferencesTitle: 'Personal preferences',
        savePreferences: 'Save preferences',
        resetPreferences: 'Reset defaults',
        preferencesSaved: 'Preferences saved',
        preferencesSavedLocally: 'Preference service unavailable; saved locally',
        preferencesReset: 'Preferences reset',
        preferencesResetLocally: 'Preference service unavailable; reset locally',
        preferenceDescription: 'Preferences are saved to the backend user profile first. Browser storage remains the offline and development fallback.',
        sections: {
          appearance: 'Appearance',
          workspace: 'Workspace'
        }
      },
      shell: {
        brandSubtitle: 'Industrial Framework',
        workspace: 'Platform Workspace',
        tenant: 'Tenant {{id}}',
        tenantFallback: 'Tenant',
        menuSearch: 'Search menu',
        noMenuResults: 'No menu results',
        globalSearch: 'Global search',
        notifications: 'Notifications',
        language: 'Language',
        collapseSidebar: 'Collapse sidebar',
        expandSidebar: 'Expand sidebar',
        resizeSidebar: 'Resize sidebar',
        profile: 'My profile',
        preferences: 'Preferences',
        commandPlaceholder: 'Search menus, routes, or features',
        languageSwitched: 'Language switched'
      },
      notifications: {
        approval: {
          title: 'Approval task pending',
          description: 'A new platform approval task is waiting for handling.'
        },
        integration: {
          title: 'Enterprise integration sync issue',
          description: 'WeCom sync has retry records. Check integration configuration.'
        },
        kanban: {
          title: 'Kanban card updated',
          description: 'A platform Kanban card has completed a status transition.'
        }
      },
      workbench: {
        title: 'Platform Workbench',
        description: 'Entry point for platform capabilities',
        platformFoundation: 'Platform Foundation',
        openKanban: 'Open Kanban',
        kanbanDescription: 'First sample for complex platform task, approval, exception, and dispatch views.',
        today: 'Today',
        open: 'Open',
        operationFocus: 'Operational Focus',
        systemHealth: 'System Health',
        quickAccess: 'Quick Access',
        metrics: {
          pendingApprovals: 'Pending approvals',
          workflowHealth: 'Workflow health',
          integrationAlerts: 'Integration alerts',
          permissionChanges: 'Permission changes'
        },
        metricHints: {
          pendingApprovals: 'Need same-day handling',
          workflowHealth: '24h success rate',
          integrationAlerts: 'Need operator review',
          permissionChanges: 'Last 7 days'
        },
        operations: {
          approval: {
            title: 'Purchase limit approval near timeout',
            description: 'A high-priority approval needs owner confirmation before purchasing execution is blocked.'
          },
          exception: {
            title: 'Shopfloor exception awaiting dispatch',
            description: 'The exception has entered platform Kanban and awaits dispatcher handling.'
          },
          sync: {
            title: 'Enterprise integration sync failed',
            description: 'WeCom synchronization has failed records; check retry policy and credentials.'
          }
        },
        health: {
          auth: 'Auth and permissions',
          workflow: 'Approval workflow',
          integration: 'Enterprise integration'
        },
        quickLinks: {
          users: 'Users',
          roles: 'Roles',
          orgs: 'Organizations',
          kanban: 'Platform Kanban'
        }
      },
      kanban: {
        title: 'Platform Kanban',
        filterKeyword: 'Search cards',
        wipNormal: 'WIP normal',
        wipOverLimit: 'WIP over limit',
        emptyColumn: 'No cards',
        views: {
          operations: 'Operations Board'
        },
        columns: {
          pending: 'Pending',
          processing: 'Processing',
          done: 'Done'
        },
        cards: {
          approval: 'Approval task pending',
          exception: 'Shopfloor exception dispatch',
          workflow: 'Workflow definition review',
          sync: 'Enterprise integration sync'
        },
        priority: {
          ALL: 'All priorities',
          HIGH: 'High',
          MEDIUM: 'Medium',
          LOW: 'Low'
        },
        sources: {
          approval: 'Approval',
          exception: 'Exception',
          workflow: 'Workflow',
          sync: 'Integration'
        },
        detail: {
          owner: 'Owner',
          source: 'Source',
          nextAction: 'Next action'
        },
        detailActions: {
          approval: 'Review approval data and field permissions, then approve, reject, or return.',
          exception: 'Confirm owner and SLA, then move the card to processing.',
          workflow: 'Complete workflow definition review, then move it to done.',
          sync: 'Check sync logs and credentials, then verify retry results.'
        }
      },
      users: {
        title: 'User Management',
        username: 'Username',
        displayName: 'Display name',
        mobile: 'Mobile',
        email: 'Email',
        primaryOrgId: 'Primary org ID',
        organizations: 'Organizations',
        primaryOrganization: 'Primary organization',
        assignOrganizations: 'Assign organizations',
        primaryOrgMustBeAssigned: 'Primary organization must be assigned',
        password: 'Initial password',
        newPassword: 'New password'
      },
      orgs: {
        title: 'Organization Management',
        orgCode: 'Org code',
        orgName: 'Org name',
        orgType: 'Org type',
        parentId: 'Parent org ID',
        sortNo: 'Sort no.',
        type: {
          COMPANY: 'Company',
          DEPARTMENT: 'Department',
          TEAM: 'Team'
        }
      },
      roles: {
        title: 'Role Permissions',
        roleCode: 'Role code',
        roleName: 'Role name',
        roleType: 'Role type',
        permissions: 'Permissions',
        assignPermissions: 'Assign permissions',
        assignMenus: 'Assign menus'
      },
      workspace: {
        refresh: 'Refresh',
        close: 'Close',
        closeOthers: 'Close Others',
        closeRight: 'Close Right',
        pin: 'Pin',
        unpin: 'Unpin',
        columnSettings: 'Column Settings',
        moveColumnUp: 'Move column up',
        moveColumnDown: 'Move column down',
        listReady: 'List ready',
        totalRecords: '{{total}} total',
        visibleColumns: '{{count}} visible columns',
        selectedRows: '{{count}} selected',
        activeFilters: '{{count}} filters',
        standardListView: 'Standard list view',
        confirmCloseTitle: 'Unsaved Changes',
        confirmCloseContent: 'Closing this tab will lose your unsaved changes. Are you sure you want to close?',
        expertMode: 'Expert Mode'
      },
      permissions: {
        groups: {
          auth: 'Auth',
          user: 'User',
          org: 'Organization',
          role: 'Role',
          menu: 'Menu',
          permission: 'Permission',
          platform: 'Platform',
          platformConfig: 'Platform Config'
        },
        platform: {
          authMe: 'View current user',
          userView: 'View users',
          userCreate: 'Create users',
          userUpdate: 'Update users',
          userDisable: 'Disable users',
          userResetPassword: 'Reset user password',
          orgView: 'View organizations',
          orgCreate: 'Create organizations',
          orgUpdate: 'Update organizations',
          roleView: 'View roles',
          roleCreate: 'Create roles',
          roleUpdate: 'Update roles',
          roleAssignPermission: 'Assign role permissions',
          roleAssignMenu: 'Assign role menus',
          menuView: 'View menus',
          menuCreate: 'Create menus',
          menuUpdate: 'Update menus',
          menuDisable: 'Disable menus',
          permissionView: 'View permissions',
          dataPermissionView: 'View data permissions',
          dataPermissionUpdate: 'Maintain data permissions',
          fieldPermissionView: 'View field permissions',
          fieldPermissionUpdate: 'Maintain field permissions',
          dictionaryView: 'View dictionaries',
          dictionaryUpdate: 'Maintain dictionaries',
          parameterView: 'View parameters',
          parameterUpdate: 'Maintain parameters',
          auditView: 'View audit logs',
          themeView: 'View theme config',
          themeUpdate: 'Maintain theme config',
          brandView: 'View brand config',
          brandUpdate: 'Maintain brand config',
          i18nView: 'View i18n resources',
          i18nUpdate: 'Maintain i18n resources',
          preferenceMe: 'Maintain own preferences'
        }
      },
      platformConfig: {
        mockFirst: 'Frontend sample',
        menuConsole: 'Menu Permission Console',
        menuSummary: 'Menu and permission mapping',
        dictionaryParameter: 'Dictionaries and Parameters',
        dictionary: 'Dictionary',
        parameter: 'Parameter',
        auditLog: 'Operation Logs',
        menuTitle: 'Menu title',
        menuCode: 'Menu code',
        menuType: 'Menu type',
        parentMenu: 'Parent menu',
        titleKey: 'Title key',
        routePath: 'Route',
        componentKey: 'Component key',
        icon: 'Icon',
        sortNo: 'Sort no.',
        visible: 'Visible',
        enabled: 'Enabled',
        hiddenMenu: 'Hidden',
        permissionCode: 'Permission code',
        dictType: 'Dictionary type',
        dictCode: 'Dictionary code',
        dictName: 'Dictionary name',
        parameterGroup: 'Parameter group',
        parameterKey: 'Parameter key',
        parameterValue: 'Parameter value',
        parameterScope: 'Scope',
        operator: 'Operator',
        module: 'Module',
        action: 'Action',
        result: 'Result',
        time: 'Time',
        auditSummary: 'Today operation records',
        results: {
          SUCCESS: 'Success',
          FAILED: 'Failed'
        }
      },
      approval: {
        title: 'Approval Task Center',
        listTitle: 'Approval task list',
        listDescription: 'Review platform approval tasks by todo, done, and started-by-me views.',
        todoCount: '{{count}} todo',
        documentNo: 'Document no.',
        taskTitle: 'Task title',
        requester: 'Requester',
        priority: 'Priority',
        fieldPermission: 'Field permissions',
        fieldPermissionDescription: 'The approval view follows field permissions. Sensitive fields will become read-only or hidden after backend integration.',
        timeline: 'Approval timeline',
        tabs: {
          todo: 'My Todo',
          done: 'Done',
          started: 'Started by Me'
        },
        actions: {
          approve: 'Approve',
          reject: 'Reject',
          return: 'Return'
        },
        feedback: {
          approved: 'Approval approved',
          rejected: 'Approval rejected',
          returned: 'Approval returned'
        },
        tasks: {
          purchaseLimit: 'Purchase limit approval',
          roleChange: 'Role permission change approval',
          integration: 'Enterprise integration config approval',
          parameter: 'System parameter change approval'
        },
        timelineItems: {
          started: 'Process started',
          waiting: 'Waiting for current node handling'
        }
      }
    }
  }
} as const;

export const initIafI18n = async (language = 'zh-CN') => {
  if (i18n.isInitialized) {
    return i18n;
  }

  await i18n.use(initReactI18next).init({
    resources,
    lng: language,
    fallbackLng: 'zh-CN',
    interpolation: {
      escapeValue: false
    }
  });

  return i18n;
};

export { i18n };
