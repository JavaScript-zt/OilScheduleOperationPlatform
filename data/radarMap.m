%�״�ͼ
data = importdata('oil.pf');
data = data(data(:,5) == 10,:);
data = data(1:100,1:4);
varNames = { 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange'};

subplot(1,2,1);
glyphplot(data,'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange'});
box on
axis off

% ���ݱ�׼��
ND = normlization(data, 2);

% ȷ���������
k = 9;

% ����
colors =  lines(k);
label = init_methods(ND, k, 2);

[data,ind] = sortrows([data,label],5);
subplot(1,2,2);
h=glyphplot(data(:,1:4),'glyph','star','varLabels',{ 'energyCost', 'pipeMixingCost', 'tankMixingCost', 'numberOfChange'},...
    'ObsLabels',num2str(ind));
for i=1:k
    hf = h(data(:,5)==i,1);
    ha = h(data(:,5)==i,2);
    for j=1:length(hf)
        hf(j).Color=colors(i,:);
        ha(j).Color=colors(i,:);
    end
end
box on
axis off

set(gcf,'Position',[600,162,900,400]);%���û�ͼ��С��λ��

%% ����Ԥ����
    % ���룺�ޱ�ǩ���ݣ���������ѡ�񷽷�
    % ����������ǩ
function label = init_methods(data, K, choose)
    if choose==1
        %�����ʼ�������ѡK����Ϊ�������ģ�����ŷ�Ͼ�����������㵽����࣬�����ݼ���ΪK�࣬���ÿ�����������ǩ
        [X_num, ~]=size(data);
        rand_array=randperm(X_num);    %����1~X_num֮���������������   
        para_miu=data(rand_array(1:K), :);  %�������ȡǰK��������X������ȡ��K����Ϊ��ʼ��������
        %ŷ�Ͼ��룬���㣨X-para_miu��^2=X^2+para_miu^2-2*X*para_miu'�������СΪX_num*K
        distant=repmat(sum(data.*data,2),1,K)+repmat(sum(para_miu.*para_miu,2)',X_num,1)-2*data*para_miu';
        %����distantÿ����Сֵ���ڵ��±�
        [~,label]=min(distant,[],2);
    elseif choose==2
        %��kmeans���г�ʼ�����࣬�����ݼ���ΪK�࣬���ÿ�����������ǩ
        label=kmeans(data, K);
    elseif choose==3
        %��FCM�㷨���г�ʼ��
        options=[NaN, NaN, NaN, 0];
        [~, responsivity]=fcm(data, K, options);   %��FCM�㷨��������Ⱦ���
        [~, label]=max(responsivity', [], 2);
    end
end

%% ���ݹ�һ��
function data = normlization(data, choose)
    if choose==0
        % ����һ��
        data = data;
    elseif choose==1
        % Z-score��һ��
        data = bsxfun(@minus, data, mean(data));
        data = bsxfun(@rdivide, data, std(data));
    elseif choose==2
        % ���-��С��һ������
        [data_num,~]=size(data);
        data=(data-ones(data_num,1)*min(data))./(ones(data_num,1)*(max(data)-min(data)));
    end
end